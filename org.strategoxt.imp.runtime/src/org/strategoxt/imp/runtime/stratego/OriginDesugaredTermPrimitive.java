package org.strategoxt.imp.runtime.stratego;

import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.library.AbstractPrimitive;
import org.spoofax.interpreter.stratego.Strategy;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.attachments.DesugaredOriginAttachment;

/**
 * Origin term after desugaring
 * @author Maartje de Jonge
 */
public class OriginDesugaredTermPrimitive extends AbstractPrimitive {
	
	public OriginDesugaredTermPrimitive() {
		super("SSL_EXT_origin_term_desugared", 0, 1);
	}

	@Override
	public boolean call(IContext env, Strategy[] svars, IStrategoTerm[] tvars) {
		if(tvars.length != 1 || svars.length !=0)
			return false;
		IStrategoTerm desugaredTerm = DesugaredOriginAttachment.getDesugaredOrigin(tvars[0]);
		if(desugaredTerm == null)
			return false;
		env.setCurrent(desugaredTerm);
		return true;
	}
}
