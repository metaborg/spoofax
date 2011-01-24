package org.strategoxt.imp.runtime.stratego;

import static org.spoofax.jsglr.client.imploder.ImploderAttachment.hasImploderOrigin;
import static org.spoofax.terms.attachments.OriginAttachment.tryGetOrigin;

import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.library.AbstractPrimitive;
import org.spoofax.interpreter.stratego.Strategy;
import org.spoofax.interpreter.terms.IStrategoTerm;

/**
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public abstract class AbstractOriginPrimitive extends AbstractPrimitive {
	
	public AbstractOriginPrimitive(String name) {
		super(name, 0, 1);
	}
	
	@Override
	public final boolean call(IContext env, Strategy[] svars, IStrategoTerm[] tvars) {
		
		if (hasImploderOrigin(tvars[0])) {
			IStrategoTerm result = call(env, tryGetOrigin(tvars[0]));
			if (result != null) {
				env.setCurrent(result);
				return true;
			}
		}
		return false;
	}
	
	protected abstract IStrategoTerm call(IContext env, IStrategoTerm origin);
}
