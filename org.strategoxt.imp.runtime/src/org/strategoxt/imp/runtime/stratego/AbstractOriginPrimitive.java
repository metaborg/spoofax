package org.strategoxt.imp.runtime.stratego;

import static org.spoofax.terms.attachments.OriginAttachment.getOrigin;

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
		
		if (getOrigin(tvars[0]) != null) {
			IStrategoTerm result = call(env, getOrigin(tvars[0]));
			if (result != null) {
				env.setCurrent(result);
				return true;
			}
		}
		return false;
	}
	
	protected abstract IStrategoTerm call(IContext env, IStrategoTerm origin);
}
