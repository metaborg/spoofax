package org.strategoxt.imp.runtime.stratego;

import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.imp.runtime.services.AutoEditStrategy;

/**
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class OriginTextPrimitive extends AbstractOriginPrimitive {

	public OriginTextPrimitive() {
		super("SSL_EXT_origin_text");
	}

	@Override
	protected IStrategoTerm call(IContext env, IStrategoTerm node) {
		String result = node.getNode().yield();
		result = AutoEditStrategy.setIndentation(result, "");
		return env.getFactory().makeString(result);
	}

}
