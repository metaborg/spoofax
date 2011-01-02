package org.strategoxt.imp.runtime.stratego;

import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.terms.ISimpleTerm;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;

/**
 * Returns the tuple (offset-start, offset-end) for an ast-node
 * @author Maartje de Jonge
 */
public class OriginOffsetPrimitive extends AbstractOriginPrimitive {

	public OriginOffsetPrimitive() {
		super("SSL_EXT_origin_offset");
	}

	@Override
	protected IStrategoTerm call(IContext env, IStrategoTerm node) {
		ITermFactory factory = env.getFactory();
		int start = getStartPosNode(node.getNode());
		int end =  getEndPosNode(node.getNode());
		return factory.makeTuple(
				factory.makeInt(start),
				factory.makeInt(end)
		);
	}
	
	private static int getStartPosNode(ISimpleTerm node){
		return node.getLeftToken().getStartOffset();//inclusive start
	}

	private static int getEndPosNode(ISimpleTerm node){
		return node.getRightToken().getEndOffset()+1; //exclusive end
	}
}
