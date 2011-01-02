package org.strategoxt.imp.runtime.stratego;

import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.terms.IStrategoTerm;

/**
 * Returns the file text
 * @author Maartje de Jonge
 */
public class OriginSourceTextPrimitive extends AbstractOriginPrimitive {

	public OriginSourceTextPrimitive() {
		super("SSL_EXT_origin_sourcetext");
	}

	@Override
	protected IStrategoTerm call(IContext env, IStrategoTerm node) {
		ILexStream lexStream = node.getNode().getLeftToken().getILexStream();
		String sourcetext=lexStream.toString(0, lexStream.getStreamLength()-1);
		return env.getFactory().makeString(sourcetext);
	}
}
