package org.strategoxt.imp.runtime.parser.ast;

import static org.spoofax.jsglr.Term.*;

import org.strategoxt.imp.runtime.parser.tokens.SGLRTokenizer;
import org.strategoxt.imp.runtime.parser.tokens.TokenKindManager;

import aterm.ATerm;
import aterm.ATermAppl;
import aterm.ATermList;
import aterm.pure.ATermListImpl;

/**
 * An asfix imploder class that can also produce ambiguous nodes.
 * 
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class AmbAsfixImploder extends AsfixImploder {

	public AmbAsfixImploder(TokenKindManager tokenManager, SGLRTokenizer tokenizer) {
		super(tokenManager, tokenizer);
	}
	
	@Override
	protected ATermAppl resolveAmbiguities(ATerm node) {
		if (!"amb".equals(((ATermAppl) node).getName()))
			return (ATermAppl) node;
		
		final ATermListImpl ambs = termAt(node, 0);
		
		ATermAppl lastNonAvoid = null;
		boolean multipleNonAvoids = false;
		
	alts:
		for (int i = 0; i < ambs.getLength(); i++) {
			ATermAppl prod = resolveAmbiguities(termAt(ambs, i));
			ATermAppl appl = termAt(prod, APPL_PROD);
			ATermAppl attrs = termAt(appl, PROD_ATTRS);
			
			if ("attrs".equals(attrs.getName())) {
				ATermList attrList = termAt(attrs, 0);
				
				for (int j = 0; j < attrList.getLength(); j++) {
					ATerm attr = termAt(attrList, j);
					if (isAppl(attr) && "prefer".equals(asAppl(attr).getName())) {
						return resolveAmbiguities(prod);
					} else if (isAppl(attr) && "avoid".equals(asAppl(attr).getName())) {
						ambs.remove(attr);
						continue alts;
					}
				}
				
				if (lastNonAvoid == null) {
					lastNonAvoid = prod;
				} else {
					multipleNonAvoids = true;
				}
			}
		}
		
		if (!multipleNonAvoids) {
			return lastNonAvoid;
		} else {
			for (int i = 0; i < ambs.getLength(); i++) { 
				ambs.setSubTerm(i, resolveAmbiguities(ambs.getSubTerm(i)));
			}
			return (ATermAppl) node;
		}
	}
	
}
