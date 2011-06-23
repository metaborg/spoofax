package org.strategoxt.imp.runtime.stratego;

import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.library.jsglr.origin.AbstractOriginPrimitive;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;

/**
 * Offset for text insertions before a list element
 * @author Maartje de Jonge
 */
public class OriginInsertBeforeOffsetPrimitive extends AbstractOriginPrimitive {
	
	public OriginInsertBeforeOffsetPrimitive() {
		super("SSL_EXT_origin_insert_before_offset");
	}

	@Override
	protected IStrategoTerm call(IContext env, IStrategoTerm origin) {
		LayoutStructure docStructure = new LayoutStructure(origin);
		ITermFactory factory = env.getFactory();		
		return	factory.makeInt(docStructure.getInsertBeforeOffset());
	}
}
