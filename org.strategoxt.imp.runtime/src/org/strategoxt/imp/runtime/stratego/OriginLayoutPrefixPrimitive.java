package org.strategoxt.imp.runtime.stratego;

import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.library.jsglr.origin.AbstractOriginPrimitive;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;

/**
 * Returns all layout directly in front of and associated with the node
 * @author Maartje de Jonge
 */
public class OriginLayoutPrefixPrimitive extends AbstractOriginPrimitive {
	
	public OriginLayoutPrefixPrimitive() {
		super("SSL_EXT_origin_layout_prefix");
	}

	@Override
	protected IStrategoTerm call(IContext env, IStrategoTerm origin) {
		LayoutStructure docStructure = new LayoutStructure(origin);
		String layoutPrefix = docStructure.getLayoutPrefix();
		ITermFactory factory = env.getFactory();		
		return 	factory.makeString(layoutPrefix);
	}
}
