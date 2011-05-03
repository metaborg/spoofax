package org.strategoxt.imp.runtime.stratego;

import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.library.jsglr.origin.AbstractOriginPrimitive;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;

/**
 * @author Maartje de Jonge
 */
public class OriginSeparatingWhitespacePrimitive extends AbstractOriginPrimitive {

	public OriginSeparatingWhitespacePrimitive() {
		super("SSL_EXT_origin_separating_whitespace");
	}

	@Override
	protected IStrategoTerm call(IContext env, IStrategoTerm origin) {
		DocumentStructure loStructure=new DocumentStructure(origin);
		ITermFactory factory = env.getFactory();
		return factory.makeString(loStructure.getSeperatingWhitespace());
	}
}
