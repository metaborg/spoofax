package org.strategoxt.imp.runtime.parser.ast;

import java.util.ArrayList;

import lpg.runtime.IToken;
import lpg.runtime.PrsStream;

import static org.spoofax.jsglr.Term.*;

import org.strategoxt.imp.runtime.Debug;
import org.strategoxt.imp.runtime.parser.SGLRParseController;
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
			!isTokenOnly && !lexicalContext ? implodeChildNodes(contents)
					                        : null;
		
		if (lexicalStart) {
			lexicalContext = false;
			IToken token = tokenizer.makeToken(offset, tokenManager.getTokenKind(lhs, rhs));

			Debug.log("Creating node ", getSort(rhs), " from ", tokenizer.dumpToString(token));	
			
			return factory.createTerminal(rhs, token);
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

	private ArrayList<SGLRAstNode> implodeChildNodes(ATermList contents) {
	    ArrayList<SGLRAstNode> result = new ArrayList<SGLRAstNode>();

		for (int i = 0; i < contents.getLength(); i++) {
			    ATerm child = contents.elementAt(i);
			    
			    if (child instanceof ATermInt) {
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
			ArrayList<SGLRAstNode> childNodes) {
		
		if (sort != null) {
			IToken left = getStartToken(prevToken);
			IToken right = tokenizer.currentToken();
			
			Debug.log("Creating node ", sort, ":", constructor, " from ", tokenizer.dumpToString(left, right));
			Debug.log("  with children: ", childNodes);
			
			return factory.createNonTerminal(sort, constructor, left, right, childNodes);
		} else {
			// TODO: Proper list recognition
			IToken left = getStartToken(prevToken);
			IToken right = tokenizer.currentToken();
			
			Debug.log("Creating node list from ", tokenizer.dumpToString(left, right));
			Debug.log("  with children: ", childNodes);
			
			return factory.createList(sort, left, right, childNodes);
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
			
			/* UNDONE: Assumed empty tokens are not supported
			if (parseStream.getSize() - index <= 1)
				throw new InvalidParseTreeException("Cannot create a AST node for an empty token");
			*/
			
			return parseStream.getTokenAt(index + 1); 
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
    	ATermAppl node = attrs;
    	
    	while (attrs.getChildCount() > 0 && isAppl(node)) {
    		node = applAt(node, 0);
    		
    		if (node.getName().equals("sort"))
    			return applAt(node, 0).getName();
    	}
    	
    	return null;
    }
}
