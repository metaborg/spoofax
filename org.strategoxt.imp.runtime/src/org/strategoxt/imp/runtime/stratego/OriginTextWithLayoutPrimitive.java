package org.strategoxt.imp.runtime.stratego;

import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.library.jsglr.origin.AbstractOriginPrimitive;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;

/**
 * Returns text of the node with associated comments (separator between node and comm after is replaced with whitespace)
 * @author Maartje de Jonge
 */
public class OriginTextWithLayoutPrimitive extends AbstractOriginPrimitive {
	
	public OriginTextWithLayoutPrimitive() {
		super("SSL_EXT_origin_text_with_layout");
	}

	@Override
	protected IStrategoTerm call(IContext env, IStrategoTerm origin) {
		LayoutStructure docStructure = new LayoutStructure(origin);
		String text = docStructure.getTextWithLayout(); 
		ITermFactory factory = env.getFactory();		
		return 	factory.makeString(text);
	}
}
