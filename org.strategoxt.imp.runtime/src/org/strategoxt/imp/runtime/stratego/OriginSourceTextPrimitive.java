package org.strategoxt.imp.runtime.stratego;

import lpg.runtime.ILexStream;

import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.imp.runtime.stratego.adapter.IWrappedAstNode;

/**
 * Returns the file text
 * @author Maartje de Jonge
 */
public class OriginSourceTextPrimitive extends AbstractOriginPrimitive {

	public OriginSourceTextPrimitive() {
		super("SSL_EXT_origin_sourcetext");
	}

	@Override
	protected IStrategoTerm call(IContext env, IWrappedAstNode node) {
		ILexStream lexStream = node.getNode().getLeftIToken().getILexStream();
		String sourcetext=lexStream.toString(0, lexStream.getStreamLength()-1);
		return env.getFactory().makeString(sourcetext);
	}
}
