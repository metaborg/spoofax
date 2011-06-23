package org.strategoxt.imp.runtime.stratego;

import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.library.jsglr.origin.AbstractOriginPrimitive;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;

/**
 * Returns (startOffset, endOffset) of the node text, 
 * inclusive layout: comments before and after + separation
 * @author Maartje de Jonge
 */
public class OriginDeletionOffsetPrimitive extends AbstractOriginPrimitive {
	
	public OriginDeletionOffsetPrimitive() {
		super("SSL_EXT_origin_deletion_offset");
	}

	@Override
	protected IStrategoTerm call(IContext env, IStrategoTerm origin) {		
		LayoutStructure docStructure = new LayoutStructure(origin);

		ITermFactory factory = env.getFactory();		
		return factory.makeTuple(
				factory.makeInt(docStructure.getDeletionStartOffset()),
				factory.makeInt(docStructure.getDeletionEndOffset())
		);
	}
}
