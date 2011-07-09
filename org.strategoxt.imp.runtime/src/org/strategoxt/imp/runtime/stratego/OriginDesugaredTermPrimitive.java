package org.strategoxt.imp.runtime.stratego;

import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.library.jsglr.origin.AbstractOriginPrimitive;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.attachments.DesugaredOriginAttachment;

/**
 * Origin term after desugaring
 * @author Maartje de Jonge
 */
public class OriginDesugaredTermPrimitive extends AbstractOriginPrimitive {
	
	public OriginDesugaredTermPrimitive() {
		super("SSL_EXT_origin_term_desugared");
	}

	@Override
	protected IStrategoTerm call(IContext env, IStrategoTerm origin) {
		return DesugaredOriginAttachment.getDesugaredOrigin(origin);
	}
}
