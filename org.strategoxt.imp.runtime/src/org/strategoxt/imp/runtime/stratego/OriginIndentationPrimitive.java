package org.strategoxt.imp.runtime.stratego;

import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.strategoxt.imp.runtime.stratego.adapter.IWrappedAstNode;

/**
 * @author Maartje de Jonge
 */
public class OriginIndentationPrimitive extends AbstractOriginPrimitive {

	public OriginIndentationPrimitive() {
		super("SSL_EXT_origin_indentation");
	}

	@Override
	protected IStrategoTerm call(IContext env, IWrappedAstNode node) {
		DocumentaryStructure loStructure=new DocumentaryStructure(node);
		ITermFactory factory = env.getFactory();
		return factory.makeString(loStructure.getIndentNode());
	}

}
