package org.strategoxt.imp.runtime.stratego;

import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.TermConverter;
import org.strategoxt.imp.runtime.stratego.adapter.IWrappedAstNode;

/**
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class OriginStripPrimitive extends AbstractOriginPrimitive {

	public OriginStripPrimitive() {
		super("SSL_EXT_origin_strip");
	}

	@Override
	protected IStrategoTerm call(IContext env, IWrappedAstNode node) {
		// We just convert it to a normal term and hope it won't
		// go original later.
		return TermConverter.convert(env.getFactory(), node);
	}

}
