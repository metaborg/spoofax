package org.strategoxt.imp.runtime.stratego;

import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.library.jsglr.origin.AbstractOriginPrimitive;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;

/**
 * Comments directly after (and associated with) the current node on the same line
 * @author Maartje de Jonge
 */
public class OriginCommentsAfterPrimitive extends AbstractOriginPrimitive {
	
	public OriginCommentsAfterPrimitive() {
		super("SSL_EXT_origin_comments_after");
	}

	@Override
	protected IStrategoTerm call(IContext env, IStrategoTerm origin) {
		LayoutStructure docStructure = new LayoutStructure(origin);
		String argumentsAfter = docStructure.getCommentsAfter();
		ITermFactory factory = env.getFactory();		
		return 	factory.makeString(argumentsAfter);
	}
}
