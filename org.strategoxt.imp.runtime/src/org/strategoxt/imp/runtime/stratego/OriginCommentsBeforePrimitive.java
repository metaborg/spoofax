package org.strategoxt.imp.runtime.stratego;

import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.library.jsglr.origin.AbstractOriginPrimitive;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;

/**
 * Comments directly before and associated with the current node
 * @author Maartje de Jonge
 */
public class OriginCommentsBeforePrimitive extends AbstractOriginPrimitive {
	
	public OriginCommentsBeforePrimitive() {
		super("SSL_EXT_origin_comments_before");
	}

	@Override
	protected IStrategoTerm call(IContext env, IStrategoTerm origin) {
		LayoutStructure docStructure = new LayoutStructure(origin);
		String layoutPrefix = docStructure.getLayoutPrefix();
		ITermFactory factory = env.getFactory();		
		return 	factory.makeString(layoutPrefix.trim());
	}
}
