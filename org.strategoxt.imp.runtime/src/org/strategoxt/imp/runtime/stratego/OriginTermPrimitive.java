package org.strategoxt.imp.runtime.stratego;

import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.terms.IStrategoTerm;

/**
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class OriginTermPrimitive extends AbstractOriginPrimitive {

	public OriginTermPrimitive() {
		super("SSL_EXT_origin_term");
	}

	@Override
	protected IStrategoTerm call(IContext env, IStrategoTerm origin) {
		return origin;
	}

}
