package org.strategoxt.imp.runtime.stratego;

import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.strategoxt.imp.runtime.stratego.adapter.IStrategoAstNode;
import org.strategoxt.imp.runtime.stratego.adapter.IWrappedAstNode;

/**
 * Returns the tuple (offset-start, offset-end) for an ast-node
 * @author Maartje de Jonge
 */
public class OriginOffsetPrimitive extends AbstractOriginPrimitive {

	public OriginOffsetPrimitive() {
		super("SSL_EXT_origin_offset");
	}

	@Override
	protected IStrategoTerm call(IContext env, IWrappedAstNode node) {
		ITermFactory factory = env.getFactory();
		int start = getStartPosNode(node.getNode());
		int end =  getEndPosNode(node.getNode());
		return factory.makeTuple(
				factory.makeInt(start),
				factory.makeInt(end)
		);
	}
	
	private static int getStartPosNode(IStrategoAstNode node){
		return node.getLeftIToken().getStartOffset();//inclusive start
	}

	private static int getEndPosNode(IStrategoAstNode node){
		return node.getRightIToken().getEndOffset()+1; //exclusive end
	}
}
