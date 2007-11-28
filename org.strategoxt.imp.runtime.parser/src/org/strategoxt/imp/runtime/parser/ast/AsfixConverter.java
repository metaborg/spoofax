package org.strategoxt.imp.runtime.parser.ast;

import java.util.ArrayList;

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
	
	private final static int EXPECTED_NODE_CHILDREN = 4;
	
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
		
		IToken prevToken = tokenizer.currentToken();
		
		boolean isTokenOnly = rhs.getName().equals("lit") || SGLRParseController.isLayout(rhs);
		
		// Enter lexical context if this is a lex node
		boolean lexicalStart = !isTokenOnly && !lexicalContext && rhs.getName().equals("lex");
		
		if (lexicalStart) lexicalContext = true;
		
		ArrayList<SGLRAstNode> childNodes =
			implodeChildNodes(contents, isTokenOnly || lexicalContext);
		
		if (lexicalStart) {
			lexicalContext = false;
			IToken token = tokenizer.makeToken(offset, tokenManager.getTokenKind(lhs, rhs), true);

			Debug.log("Creating node ", getSort(rhs), " from ", tokenizer.dumpToString(token));	
			
			return factory.createTerminal(getSort(rhs), token);
		} else if (lexicalContext) {
			return null; // don't create tokens inside lexical context; just create one big token at the top
		} else if (isTokenOnly) {
			tokenizer.makeToken(offset, tokenManager.getTokenKind(lhs, rhs));
			return null;
		} else {
			String constructor = getConstructor(attrs);
			String sort = getSort(rhs);
			
			tokenizer.makeToken(offset, tokenManager.getTokenKind(lhs, rhs));
			return implodeContextFree(sort, constructor, prevToken, childNodes);
		}
	}

	private ArrayList<SGLRAstNode> implodeChildNodes(ATermList contents, boolean tokensOnly) {
	    ArrayList<SGLRAstNode> result =
	    	tokensOnly ? null : new ArrayList<SGLRAstNode>(EXPECTED_NODE_CHILDREN);

		for (int i = 0; i < contents.getLength(); i++) {
			    ATerm child = contents.elementAt(i);
			    
			    if (isInt(child)) {
			    	implodeLexical(child);
			    } else {
			    	// Recurse
			    	SGLRAstNode childNode = implodeAppl(ignoreAmb(child));
			    	
			    	if (childNode != null) result.add(childNode);
			    }
		}
		
	    return result;
    }

	/** Implode a context-free node. */
	private SGLRAstNode implodeContextFree(String sort, String constructor, IToken prevToken,
			ArrayList<SGLRAstNode> children) {
		
		IToken left = getStartToken(prevToken);
		IToken right = tokenizer.currentToken();
		
		if (Debug.ENABLED) {
			String name = sort == null ? "list" : sort;
			Debug.log("Creating node ", name, ":", constructor, SGLRAstNode.getSorts(children), " from ", tokenizer.dumpToString(left, right));
		}
		
		if (constructor == null && children.size() == 1 && children.get(0).getSort() == SGLRAstNode.STRING_SORT) {
			// TODO: Is this right? First create a <string> node, put it in a list, and then dispose it?
			assert left == right;
			return factory.createTerminal(sort, left);
		}
		
		if (sort != null) {
			return factory.createNonTerminal(sort, constructor, left, right, children);
		} else {
			// TODO: Proper list recognition
			
			return factory.createList(sort, left, right, children);
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
				// HACK: Assume TK_KEYWORD kind for empty tokens in AST nodes
				return tokenizer.makeToken(offset, SGLRParsersym.TK_KEYWORD, true);
			} else {
				return parseStream.getTokenAt(index + 1); 
			}
		}
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

    private static String getSort(ATermAppl attrs) {
    	ATerm node = attrs;
    	
    	while (node.getChildCount() > 0 && isAppl(node)) {
    		if (asAppl(node).getName().equals("sort"))
    			return applAt(node, 0).getName();
    		
    		node = termAt(node, 0);
    	}
    	
    	return null;
    }
}
