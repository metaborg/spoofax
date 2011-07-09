package org.strategoxt.imp.runtime.stratego;

import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.library.AbstractPrimitive;
import org.spoofax.interpreter.stratego.Strategy;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.jsglr.client.imploder.ImploderAttachment;
import org.spoofax.terms.StrategoAppl;
import org.spoofax.terms.attachments.OriginAttachment;
import org.spoofax.terms.attachments.ParentAttachment;

/**
 * Returns what is most probably the origin term (retrieved from inspecting the subterms) 
 * 
 * @author Maartje de Jonge
 */
public class OriginTermFuzzyPrimitive extends AbstractPrimitive {

	public OriginTermFuzzyPrimitive() {
		super("SSL_EXT_origin_term_fuzzy", 0, 1);
	}

	@Override
	public boolean call(IContext env, Strategy[] svars, IStrategoTerm[] tvars) {
		if(tvars.length != 1 || svars.length !=0)
			return false;
		IStrategoTerm term = tvars[0];
		IStrategoTerm originTerm = OriginAttachment.getOrigin(term);
		if(originTerm != null){
			env.setCurrent(originTerm);
			return true; //fuzzy origin term not needed
		}
		if(!(term instanceof StrategoAppl)){
			return false; //only fuzzy origin term for appl terms
		}
		for (int i = 0; i < term.getSubtermCount(); i++) {
			IStrategoTerm originSubTerm = OriginAttachment.getOrigin(term.getSubterm(i));
			if(originSubTerm != null){
				IStrategoTerm parent = ParentAttachment.getParent(originSubTerm);
				if(isOriginRelatedFuzzy(term, parent)){
					env.setCurrent(parent); //Term with the same signature and at least one origin related child 
					return true;
				}				
			}
		}
		return false;
	}

	private boolean isOriginRelatedFuzzy(IStrategoTerm term, IStrategoTerm parent) {
		return 
			parent != null &&
			ImploderAttachment.hasImploderOrigin(parent) &&
			parent.getSubtermCount() == term.getSubtermCount() &&
			parent instanceof StrategoAppl &&
			term instanceof StrategoAppl &&
			((StrategoAppl)parent).getConstructor().equals(((StrategoAppl)term).getConstructor());
	}	
}
