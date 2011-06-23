package org.strategoxt.imp.runtime.stratego;

import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.library.jsglr.origin.AbstractOriginPrimitive;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;

/**
 * Returns separation between first and second element of the (parent) list.
 * Returns null in case node is not a list fragment or parent list has fewer then 2 elements
 * @author Maartje de Jonge
 */
public class OriginSeparationPrimitive extends AbstractOriginPrimitive {

	public OriginSeparationPrimitive() {
		super("SSL_EXT_origin_separation");
	}

	@Override
	protected IStrategoTerm call(IContext env, IStrategoTerm origin) {
		LayoutStructure docStructure = new LayoutStructure(origin);
		String separation = docStructure.getSeparation();
		ITermFactory factory = env.getFactory();		
		return factory.makeString(separation);
	}
}
