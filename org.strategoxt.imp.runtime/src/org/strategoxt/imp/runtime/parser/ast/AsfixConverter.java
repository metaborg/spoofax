package org.strategoxt.imp.runtime.parser.ast;

import java.util.ArrayList;
import static java.lang.Math.min;

import lpg.runtime.IToken;
import lpg.runtime.PrsStream;

import static org.spoofax.jsglr.Term.*;

import org.strategoxt.imp.runtime.Debug;
import org.strategoxt.imp.runtime.parser.SGLRParseController;
import org.strategoxt.imp.runtime.parser.tokens.SGLRParsersym;
import org.strategoxt.imp.runtime.parser.tokens.SGLRTokenKindManager;
import org.strategoxt.imp.runtime.parser.tokens.SGLRTokenizer;

import aterm.ATerm;
import aterm.ATermAppl;
import aterm.ATermInt;
import aterm.ATermList;
import aterm.pure.ATermListImpl;

/**
 * Class to convert an Asfix tree to another format.
 * 
 * @author Lennart Kats <L.C.L.Kats add tudelft.nl>
 */
public class AsfixConverter {
	private final static int PARSE_TREE = 0;
	
	private final static int APPL_PROD = 0;
	
	private final static int APPL_CONTENTS = 1;

	private final static int PROD_LHS = 0;
	
	private final static int PROD_RHS = 1;

	private final static int PROD_ATTRS = 2;

	private final static int ATTRS_LIST = 0;

	private final static int AMB_LIST = 0;
	
	private final static int TERM_CONS = 0;
	
	private final static int CONS_NAME = 0;
	
	private final static int PARAMETRIZED_SORT_NAME = 0;
	
	private final static int PARAMETRIZED_SORT_ARGS = 1;
	
	private final static int ALT_SORT_LEFT = 0;
	
	private final static int ALT_SORT_RIGHT = 1;
	
	private final static int EXPECTED_NODE_CHILDREN = 5;
	
	private final SGLRAstNodeFactory<SGLRAstNode> factory;
	
	private final SGLRTokenizer tokenizer;
	
	private final SGLRTokenKindManager tokenManager;
	
	/** Character offset for the current implosion. */ 
	private int offset;
	
	private boolean lexicalContext;
	
	@SuppressWarnings("unchecked") // TODO2: Expand/explode generic signatures?	
    public AsfixConverter(SGLRAstNodeFactory factory, SGLRTokenKindManager tokenManager, SGLRTokenizer tokenizer) {
		this.factory = factory;
		this.tokenManager = tokenManager;
		this.tokenizer = tokenizer;
	}
	
	public SGLRAstNode implode(ATerm asfix) {
		if (!(asfix instanceof ATermAppl && ((ATermAppl) asfix).getName().equals("parsetree")))
			throw new IllegalArgumentException("Parse tree expected");
		
		ATerm top = (ATerm) asfix.getChildAt(PARSE_TREE);
		offset = 0;
		lexicalContext = false;
		
		SGLRAstNode root = implodeAppl(ignoreAmb(top));

		tokenizer.endStream();
		
		return root;
	}
	
	/** Implode any appl(_, _). */
	private SGLRAstNode implodeAppl(ATermAppl appl) {
		ATermAppl prod = termAt(appl, APPL_PROD);
		ATermList lhs = termAt(prod, PROD_LHS);
		ATermAppl rhs = termAt(prod, PROD_RHS);
		ATermAppl attrs = termAt(prod, PROD_ATTRS);
		ATermList contents = termAt(appl, APPL_CONTENTS);
		
		// TODO: Support <int> nodes in implosion (e.g., chars in Java)
		
		IToken prevToken = tokenizer.currentToken();
		
		// Enter lexical context if this is a lex node
		boolean lexicalStart = !lexicalContext
			&& (rhs.getName().equals("lex") || rhs.getName().equals("lit")
			    || SGLRParseController.isLayout(rhs));
		
		if (lexicalStart) lexicalContext = true;
		
		boolean isList = !lexicalContext && SGLRParseController.isList(rhs);
		
		// Recurse the tree (and set children if applicable)
		ArrayList<SGLRAstNode> children =
			implodeChildNodes(contents, lexicalContext);
		
		if (lexicalStart) {
			lexicalContext = false;
			IToken token = tokenizer.makeToken(offset, tokenManager.getTokenKind(lhs, rhs), true);
			String sort = getSort(rhs);
			
			if (sort == null) return null;
			
			Debug.log("Creating node ", getSort(rhs), " from ", SGLRTokenizer.dumpToString(token));	
			
			return factory.createTerminal(getSort(rhs), token);
		} else if (lexicalContext) {
			return null; // don't create tokens inside lexical context; just create one big token at the top
		} else {
			String constructor = getConstructor(attrs);
			String sort = getSort(rhs);
			
			tokenizer.makeToken(offset, tokenManager.getTokenKind(lhs, rhs));
			return implodeContextFree(sort, constructor, prevToken, children, isList);
		}
	}

	private ArrayList<SGLRAstNode> implodeChildNodes(ATermList contents, boolean tokensOnly) {
		
		ArrayList<SGLRAstNode> result = tokensOnly
				? null
				: new ArrayList<SGLRAstNode>(
						min(EXPECTED_NODE_CHILDREN, contents.getChildCount()));

		for (int i = 0; i < contents.getLength(); i++) {
			ATerm child = contents.elementAt(i);

			if (isInt(child)) {
				implodeLexical(child);
			} else {
				// Recurse
				SGLRAstNode childNode = implodeAppl(ignoreAmb(child));

				if (childNode != null)
					result.add(childNode);
			}
		}

		return result;
	}

	/** Implode a context-free node. */
	private SGLRAstNode implodeContextFree(String sort, String constructor, IToken prevToken,
			ArrayList<SGLRAstNode> children, boolean isList) {
		
		IToken left = getStartToken(prevToken);
		IToken right = getEndToken(left, tokenizer.currentToken());
		
		if (Debug.ENABLED) {
			String name = isList ? "list" : sort;
			Debug.log("Creating node ", name, ":", constructor, SGLRAstNode.getSorts(children), " from ", SGLRTokenizer.dumpToString(left, right));
		}
		
		if (isList) {
			return factory.createList(sort, left, right, children);
		} else if (constructor == null && children.size() == 1 && children.get(0).getSort() == SGLRAstNode.STRING_SORT) {
			// Child node was a <string> node (rare case); unpack it and create a new terminal
			assert left == right && children.get(0).getChildren().size() == 0;
			return factory.createTerminal(sort, left);
		} else {
			return factory.createNonTerminal(sort, constructor, left, right, children);
		}
	}
	
	/** Ignore any ambiguities in the parse tree. */
	private static ATermAppl ignoreAmb(ATerm node) {
		ATermAppl appl = (ATermAppl) node;
		
		if (appl.getName().equals("amb")) {
			// TODO: Do something with ambiguities?			
			ATermListImpl ambs = (ATermListImpl) appl.getChildAt(AMB_LIST);
			return ignoreAmb(ambs.getFirst());
		} else {
			return appl;
		}
	}
	
	/** Get the token after the previous node's ending token, or null if N/A. */
	private IToken getStartToken(IToken prevToken) {
		PrsStream parseStream = tokenizer.getParseStream();
		
		if (prevToken == null) {
			return parseStream.getSize() == 0 ? null
			                                  : parseStream.getTokenAt(0);
		} else {
			int index = prevToken.getTokenIndex();
			
			if (parseStream.getSize() - index <= 1) {
				// UNDONE: Assumed empty tokens were harmful
				// throw new InvalidParseTreeException("Cannot create a AST node for an empty token");

				// Create new empty token
				// HACK: Assume TK_LAYOUT kind for empty tokens in AST nodes
				return tokenizer.makeToken(offset, SGLRParsersym.TK_LAYOUT, true);
			} else {
				return parseStream.getTokenAt(index + 1); 
			}
		}
	}
	
	/** Get the last no-layout token for an AST node. */
	private IToken getEndToken(IToken startToken, IToken lastToken) {
		PrsStream parseStream = tokenizer.getParseStream();
		int begin = startToken.getTokenIndex();
		
		for (int i = lastToken.getTokenIndex(); i > begin; i--) {
			lastToken = parseStream.getTokenAt(i);
			if (lastToken.getKind() != SGLRParsersym.TK_LAYOUT
					|| lastToken.getStartOffset() == lastToken.getEndOffset()-1)
				break;
		}
		
		return lastToken;
	}
	
	/** Implode any appl(_, _) that constructs a lex terminal. */
	private void implodeLexical(ATerm character) {		
		// Add character
		assert ((ATermInt) character).getInt()
			== tokenizer.getLexStream().getCharValue(offset)
			: "Character from asfix stream must be in lex stream";
		offset++;
	}
	
	/** Return the contents of the cons() attribute, or null if not found. */
	private static String getConstructor(ATermAppl attrs) {
		if (attrs.getName().equals("no-attrs"))
			return null;
		
		ATermList list = (ATermList) attrs.getChildAt(ATTRS_LIST);
		
		for (int i = 0; i < list.getLength(); i++) {
			ATerm attr = list.elementAt(i);
			
			if (attr instanceof ATermAppl) {
				ATermAppl namedAttr = (ATermAppl) attr;
				if (namedAttr.getName().equals("term")) {
					namedAttr = (ATermAppl) namedAttr.getChildAt(TERM_CONS);
					
					if (namedAttr.getName().equals("cons")) {
						namedAttr = (ATermAppl) namedAttr.getChildAt(CONS_NAME);
						return namedAttr.getName();
					}
				}				
			}
		}
		
		return null; // no cons found
	}

	// TODO2: Optimize - cache getSort (especially for parametrized-sort!)
	/** 
	 * Get the RTG sort name of a production RHS, or for lists, the RTG element sort name.
	 */
    private static String getSort(ATermAppl rhs) {
    	ATerm node = rhs;
    	
    	while (node.getChildCount() > 0 && isAppl(node)) {
    		if (asAppl(node).getName().equals("sort"))
    			return applAt(node, 0).getName();
    		if (asAppl(node).getName().equals("alt"))
    			return getAltSortName(node);
    		if (asAppl(node).getName().equals("parameterized-sort"))
    			return getParameterizedSortName(node);
    		
    		node = termAt(node, 0);
    	}
    	
    	return null;
    }
    
    private static String getParameterizedSortName(ATerm node) {
    	StringBuilder result = new StringBuilder();
    	
    	result.append(applAt(node, PARAMETRIZED_SORT_NAME).getName());
    	result.append('_');
    	
		ATermList args = termAt(node, PARAMETRIZED_SORT_ARGS);
		
		for (int i = 0; i < args.getChildCount(); i++) {
			ATermAppl arg = termAt(termAt(args, i), 0);
			result.append(arg.getName());
		}
		
		return result.toString();
    }
    
    private static String getAltSortName(ATerm node) {
		String left = getSort(applAt(node, ALT_SORT_LEFT));
		String right = getSort(applAt(node, ALT_SORT_RIGHT));
		
		// HACK: In the RTG, alt sorts appear with a number at the end
		return left + "_" + right + "0";
    }
}
