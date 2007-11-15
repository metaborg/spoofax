package org.strategoxt.imp.runtime.parser.ast;

import java.util.ArrayList;

import lpg.runtime.IToken;
import lpg.runtime.PrsStream;

import org.strategoxt.imp.runtime.Debug;
import org.strategoxt.imp.runtime.parser.SGLRTokenizer;

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
	private final SGLRAstNodeFactory factory;
	
	private final SGLRTokenizer tokenizer;
	
	private final static int PARSE_TREE = 0;
	
	private final static int APPL_PROD = 0;
	
	private final static int APPL_CONTENTS = 1;

	// private final static int PROD_LHS = 0;
	
	private final static int PROD_RHS = 1;

	private final static int PROD_ATTRS = 2;

	private final static int ATTRS_LIST = 0;

	private final static int AMB_LIST = 0;
	
	private final static int TERM_CONS = 0;
	
	private final static int CONS_NAME = 0;
	
	/** Character offset for the current implosion. */ 
	private int offset;
	
	private boolean lexicalContext;
	
	public AsfixConverter(SGLRAstNodeFactory factory, SGLRTokenizer tokenizer) {
		this.factory = factory;
		this.tokenizer = tokenizer;
	}
	
	public SGLRAstNode implode(ATerm asfix) {
		if (!(asfix instanceof ATermAppl && ((ATermAppl) asfix).getName().equals("parsetree")))
			throw new IllegalArgumentException("Parse tree expected");
		
		ATerm top = (ATerm) asfix.getChildAt(PARSE_TREE);
		offset = 0;
		lexicalContext = false;
		
		return implodeAppl(ignoreAmb(top));
	}
	
	/** Implode any appl(_, _). */
	private SGLRAstNode implodeAppl(ATermAppl appl) {
		ATermAppl prod = (ATermAppl) appl.getChildAt(APPL_PROD);
		ATermAppl rhs = (ATermAppl) prod.getChildAt(PROD_RHS);
		ATermAppl attrs = (ATermAppl) prod.getChildAt(PROD_ATTRS);
		ATermList contents = (ATermList) appl.getChildAt(APPL_CONTENTS);
		
		IToken prevToken = tokenizer.currentToken();

		// TODO2: Optimization; don't need to always allocate child list
		ArrayList<SGLRAstNode> childNodes = new ArrayList<SGLRAstNode>();
		
		for (int i = 0; i < contents.getLength(); i++) {
			ATerm child = contents.elementAt(i);
			
			if (child instanceof ATermInt) {
				implodeLexical(child);
			} else {
				// Recurse
				SGLRAstNode childNode = implodeAppl(ignoreAmb(child));
				
				if (childNode != null) childNodes.add(childNode);
			}
		}
		
		if (lexicalContext)
			return null; // don't create tokens in lexical context

		IToken token = tokenizer.makeToken(offset, factory.getTokenKind(rhs));
		
		if (rhs.getName().equals("lex")) {
			return factory.createTerminal(token);
		} else {			
			return implodeContextFree(getConstructor(attrs), getStartToken(prevToken), childNodes);
		}
	}

	/** Implode a context-free node. */
	private SGLRAstNode implodeContextFree(String constructor, IToken startToken,
			ArrayList<SGLRAstNode> childNodes) {
		
		if (constructor != null) {
			return factory.createNonTerminal(constructor, startToken,tokenizer.currentToken(),
			                                 childNodes);
		} else {
			switch (childNodes.size()) {
				case 0:
					return null;
				case 1:
					return childNodes.get(0);
				default:
					return factory.createList(startToken, tokenizer.currentToken(),
					                          childNodes);
			}
		}
	}
	
	/** Ignore any ambiguities in the parse tree. */
	private static ATermAppl ignoreAmb(ATerm node) {
		ATermAppl appl = (ATermAppl) node;
		
		if (appl.getName().equals("amb")) {
			// TODO: Do something with ambiguities?
			Debug.log("Ambiguity in parse tree: ", node);
			
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
			
			return parseStream.getSize() <= index ? null
			                                      : parseStream.getTokenAt(index + 1); 
		}
	}
	
	
	/** Implode any appl(_, _) that constructs a lex terminal. */
	private void implodeLexical(ATerm character) {		
		// Add character
		assert ((ATermInt) character).getInt()
			== tokenizer.getLexStream().getCharValue(offset)
			: "Character from asfix stream must be in lex stream";
		assert false;
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
					
					if (namedAttr.getName().equals("cons"))
						return namedAttr.getChildAt(CONS_NAME).toString();
				}
				
			}
		}
		
		return null; // no cons found
	}
}
