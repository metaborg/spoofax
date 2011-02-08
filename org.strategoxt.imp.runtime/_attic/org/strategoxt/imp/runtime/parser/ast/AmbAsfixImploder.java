package org.strategoxt.imp.runtime.parser.ast;

import static org.spoofax.terms.Term.termAt;
import static org.strategoxt.imp.runtime.parser.ast.AsfixAnalyzer.AMB_FUN;

import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.imp.runtime.parser.tokens.TokenKindManager;

import aterm.ATermAppl;

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
	 * @see org.strategoxt.imp.runtime.parser.ast.AsfixImploder#resolveAmbiguities(org.spoofax.interpreter.terms.IStrategoTerm)
	 */
	@Override
	protected ATermAppl resolveAmbiguities(IStrategoTerm node) {
		// TODO: disable when prefer/avoid disambiguation works in Disambiguator
		if (AMB_FUN != ((ATermAppl) node).getIStrategoConstructor())
			return (ATermAppl) node;
		
		final IStrategoList ambs = termAt(node, 0);
		
	alts:
		for (int i = 0; i < ambs.size(); i++) {
			ATermAppl amb = resolveAmbiguities(termAt(ambs, i));
			ambs.setSubTerm(i, amb);
			
			if (AMB_FUN != amb.getIStrategoConstructor()) {
	            ATermAppl appl = termAt(amb, APPL_PROD);
	            ATermAppl attrs = termAt(appl, PROD_ATTRS);
	            
	            if ("attrs".equals(attrs.getName())) {
	                IStrategoList attrList = termAt(attrs, 0);
	                
	                for (int j = 0; j < attrList.getLength(); j++) {
	                    IStrategoTerm attr = termAt(attrList, j);
	                    if (isTermAppl(attr) && "prefer".equals(asAppl(attr).getName())) {
	                        return resolveAmbiguities(amb);
	                    } else if (isTermAppl(attr) && "avoid".equals(asAppl(attr).getName())) {
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
