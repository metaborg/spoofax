package org.strategoxt.imp.runtime.stratego;

import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.jsglr.client.imploder.IToken;

/**
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class OriginLocationPrimitive extends AbstractOriginPrimitive {

	public OriginLocationPrimitive() {
		super("SSL_EXT_origin_location");
	}

	@Override
	protected IStrategoTerm call(IContext env, IStrategoTerm node) {
		ITermFactory factory = env.getFactory();
		IToken start = node.getNode().getLeftToken();
		IToken end = node.getNode().getRightToken();  
		return factory.makeTuple(
				factory.makeInt(start.getLine()),
				factory.makeInt(start.getColumn()),
				factory.makeInt(end.getEndLine()),
				factory.makeInt(end.getEndColumn()));
	}

}
