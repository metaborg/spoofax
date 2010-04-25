package org.strategoxt.imp.runtime.stratego;

import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.strategoxt.imp.runtime.stratego.adapter.IWrappedAstNode;

/**
 * Returns the tuple (offset-start, offset-end) for an ast-node
 * @author Maartje de Jonge
 */
public class OriginCharPositionPrimitive extends AbstractOriginPrimitive {

	public OriginCharPositionPrimitive() {
		super("SSL_EXT_origin_char_position");
	}

	@Override
	protected IStrategoTerm call(IContext env, IWrappedAstNode node) {
		ITermFactory factory = env.getFactory();
		int start = TextPositions.getStartPosNode(node.getNode());
		int end =  TextPositions.getEndPosNode(node.getNode());
		return factory.makeTuple(
				factory.makeInt(start),
				factory.makeInt(end)
		);
	}

}
