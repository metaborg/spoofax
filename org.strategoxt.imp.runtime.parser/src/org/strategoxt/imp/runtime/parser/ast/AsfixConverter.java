package org.strategoxt.imp.runtime.parser.ast;

import org.spoofax.interpreter.adapter.aterm.WrappedATermFactory;
import org.spoofax.jsglr.InvalidParseTableException;
import org.strategoxt.imp.runtime.parser.SGLRTokenizer;

import lpg.runtime.IPrsStream;
import lpg.runtime.PrsStream;
import aterm.ATerm;
import aterm.ATermAppl;
import aterm.ATermInt;
import aterm.ATermList;

/**
 * Class to convert an Asfix tree to another format.
 * 
 * @author Lennart Kats <L.C.L.Kats add tudelft.nl>
 */
public class AsfixConverter {
	private final WrappedATermFactory factory;
	
	private final static int PARSE_TREE = 0;
	
	private final static int APPL_PROD = 0;
	
	private final static int APPL_CONTENTS = 1;

	private final static int PROD_ATTRS = 2;

	private final static int ATTRS_LIST = 0;
	
	private final static int CONS_NAME = 0;
	
	public AsfixConverter(WrappedATermFactory factory) {
		this.factory = factory;
	}
	
	public ATermAstNode implode(ATerm asfix, SGLRTokenizer tokenizer) {
		final StringBuilder token = new StringBuilder();
		
		if (!(asfix instanceof ATermAppl && ((ATermAppl) asfix).getName().equals("parsetree")))
			throw new IllegalArgumentException("Parse tree expected");
		
		ATerm top = (ATerm) asfix.getChildAt(PARSE_TREE);
		
		return implodeAppl(top, tokenizer);
	}
	
	private ATermAstNode implodeAppl(ATerm appl, SGLRTokenizer tokenizer) {
		ATermAppl attrs = (ATermAppl) appl.getChildAt(APPL_PROD).getChildAt(PROD_ATTRS);
		ATermList contents = (ATermList) appl.getChildAt(APPL_CONTENTS);
 		
		String cons = getCons(attrs);
		
		if (contents.getLength() == 0 || contents.elementAt(0) instanceof ATermAppl) {
			// Recurse
			for (int i = 0; i < contents.getLength(); i++) {
				ATerm child = contents.elementAt(i);
				implodeAppl(child, tokenizer);
			}
		} else {
			// Add token
			tokenizer.add(contents.getLength(), 0);
		}
	}
	
	/** Return the contents of the cons() attribute, or null if not found. */
	private static String getCons(ATermAppl attrs) {
		if (attrs.getName().equals("no-attrs"))
			return null;
		
		ATermList list = (ATermList) attrs.getChildAt(ATTRS_LIST);
		
		for (int i = 0; i < list.getLength(); i++) {
			ATerm attr = list.elementAt(i);
			if (attr instanceof ATermAppl) {
				ATermAppl namedAttr = (ATermAppl) attr;
				if (namedAttr.getName().equals("cons"))
					return namedAttr.getChildAt(CONS_NAME).toString();
			}
		}
		
		return null; // no cons found
	}
}
