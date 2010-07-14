package org.strategoxt.imp.runtime.stratego;

import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.strategoxt.imp.runtime.stratego.adapter.IWrappedAstNode;

/**
 * @author Maartje de Jonge
 */
public class OriginOffsetWithLayoutPrimitive extends AbstractOriginPrimitive {

	public OriginOffsetWithLayoutPrimitive() {
		super("SSL_EXT_origin_offset_with_layout");
	}

	@Override
	protected IStrategoTerm call(IContext env, IWrappedAstNode node) {
		DocumentaryStructure loStructure=new DocumentaryStructure(node);
		ITermFactory factory = env.getFactory();
		return factory.makeTuple(
				factory.makeInt(loStructure.textWithLayout().getStart()),
				factory.makeInt(loStructure.textWithLayout().getEnd())
		);
	}

}
