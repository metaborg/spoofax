package org.strategoxt.imp.runtime.parser.ast;

import static org.spoofax.jsglr.Term.asAppl;
import static org.spoofax.jsglr.Term.isAppl;
import static org.spoofax.jsglr.Term.termAt;

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

	public AmbAsfixImploder(TokenKindManager tokenManager) {
		super(tokenManager);
	}
	
	/**
	 * Resolve or ambiguities with avoid/prefer annotations.
	 * 
	 * @see org.strategoxt.imp.runtime.parser.ast.AsfixImploder#resolveAmbiguities(aterm.ATerm)
	 */
	@Override
	protected ATermAppl resolveAmbiguities(ATerm node) {
		// TODO: disable when prefer/avoid disambiguation works in Disambiguator
		if (!"amb".equals(((ATermAppl) node).getName()))
			return (ATermAppl) node;
		
		final ATermListImpl ambs = termAt(node, 0);
		
	alts:
		for (int i = 0; i < ambs.getLength(); i++) {
			ATermAppl amb = resolveAmbiguities(termAt(ambs, i));
			ambs.setSubTerm(i, amb);
			
			if (!amb.getName().equals("amb")) {
	            ATermAppl appl = termAt(amb, APPL_PROD);
	            ATermAppl attrs = termAt(appl, PROD_ATTRS);
	            
	            if ("attrs".equals(attrs.getName())) {
	                ATermList attrList = termAt(attrs, 0);
	                
	                for (int j = 0; j < attrList.getLength(); j++) {
	                    ATerm attr = termAt(attrList, j);
	                    if (isAppl(attr) && "prefer".equals(asAppl(attr).getName())) {
	                        return resolveAmbiguities(amb);
	                    } else if (isAppl(attr) && "avoid".equals(asAppl(attr).getName())) {
	                        ambs.remove(amb);
	                        continue alts;
	                    }
	                }
	            }				
			}
		}
		
		// TODO: Throw away ambs in lexical context??
		
		return ambs.getLength() == 1
				? (ATermAppl) ambs.getSubTerm(0)
				: (ATermAppl) node;
	}
	
}
