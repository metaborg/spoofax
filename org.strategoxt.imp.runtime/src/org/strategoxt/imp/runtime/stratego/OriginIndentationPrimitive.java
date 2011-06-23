package org.strategoxt.imp.runtime.stratego;

import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.library.jsglr.origin.AbstractOriginPrimitive;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;

/**
 * Returns indentation of the line where the node starts 
 * @author Maartje de Jonge
 */
public class OriginIndentationPrimitive extends AbstractOriginPrimitive {

	public OriginIndentationPrimitive() {
		super("SSL_EXT_origin_indentation");
	}

	@Override
	protected IStrategoTerm call(IContext env, IStrategoTerm origin) {
		LayoutStructure loStructure = new LayoutStructure(origin);
		ITermFactory factory = env.getFactory();
		return factory.makeString(loStructure.getIndentation());
	}

}
