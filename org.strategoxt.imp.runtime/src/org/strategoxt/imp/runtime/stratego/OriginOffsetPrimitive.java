package org.strategoxt.imp.runtime.stratego;

import static org.spoofax.jsglr.client.imploder.ImploderAttachment.getLeftToken;
import static org.spoofax.jsglr.client.imploder.ImploderAttachment.getRightToken;

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
	protected IStrategoTerm call(IContext env, IStrategoTerm origin) {
		ITermFactory factory = env.getFactory();
		int start = getStartPosNode(origin);
		int end =  getEndPosNode(origin);
		return factory.makeTuple(
				factory.makeInt(start),
				factory.makeInt(end)
		);
	}
	
	private static int getStartPosNode(ISimpleTerm node){
		return getLeftToken(node).getStartOffset();//inclusive start
	}

	private static int getEndPosNode(ISimpleTerm node){
		// XXX: why deviate from the normal inclusive end offset convention??
		return getRightToken(node).getEndOffset()+1; //exclusive end
	}
}
