package org.strategoxt.imp.runtime.stratego;

import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.library.jsglr.origin.AbstractOriginPrimitive;
import org.spoofax.interpreter.terms.IStrategoTerm;

/**
 * Offset for text insertions at the end of a list
 * @author Maartje de Jonge
 */
public class OriginInsertAtEndOffsetPrimitive extends AbstractOriginPrimitive {
	
	public OriginInsertAtEndOffsetPrimitive() {
		super("SSL_EXT_origin_insert_at_end_offset");
	}

	@Override
	protected IStrategoTerm call(IContext env, IStrategoTerm origin) {
		LayoutStructure docStructure = new LayoutStructure(origin);
		return	env.getFactory().makeInt(docStructure.getInsertAtEndOffset());
	}
}
