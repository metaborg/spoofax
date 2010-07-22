package org.strategoxt.imp.runtime.stratego;

import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.strategoxt.imp.runtime.stratego.adapter.IWrappedAstNode;

/**
 * @author Maartje de Jonge
 */
public class OriginSeparatingWhitespacePrimitive extends AbstractOriginPrimitive {

	public OriginSeparatingWhitespacePrimitive() {
		super("SSL_EXT_origin_separating_whitespace");
	}

	@Override
	protected IStrategoTerm call(IContext env, IWrappedAstNode node) {
		DocumentStructure loStructure=new DocumentStructure(node);
		ITermFactory factory = env.getFactory();
		return factory.makeString(loStructure.getSeperatingWhitespace());
	}

}
