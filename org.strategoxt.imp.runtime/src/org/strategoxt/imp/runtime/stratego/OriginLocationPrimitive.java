package org.strategoxt.imp.runtime.stratego;

import lpg.runtime.IToken;

import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.strategoxt.imp.runtime.stratego.adapter.IWrappedAstNode;

/**
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class OriginLocationPrimitive extends AbstractOriginPrimitive {

	public OriginLocationPrimitive() {
		super("SSL_EXT_origin_location");
	}

	@Override
	protected IStrategoTerm call(IContext env, IWrappedAstNode node) {
		ITermFactory factory = env.getFactory();
		IToken start = node.getNode().getLeftIToken();
		IToken end = node.getNode().getRightIToken(); //FIXME: getRightIToken() gives the first token of the last LIST element 
		return factory.makeTuple(
				factory.makeInt(start.getLine()),
				factory.makeInt(start.getColumn()),
				factory.makeInt(end.getEndLine()),
				factory.makeInt(end.getEndColumn()));
	}

}
