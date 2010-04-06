package org.strategoxt.imp.runtime.stratego;

import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.library.AbstractPrimitive;
import org.spoofax.interpreter.stratego.Strategy;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.imp.runtime.stratego.adapter.IWrappedAstNode;

/**
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public abstract class AbstractOriginPrimitive extends AbstractPrimitive {
	
	public AbstractOriginPrimitive(String name) {
		super(name, 0, 1);
	}
	
	@Override
	public final boolean call(IContext env, Strategy[] svars, IStrategoTerm[] tvars) {
		
		if (tvars[0] instanceof IWrappedAstNode) {
			IStrategoTerm result = call(env, (IWrappedAstNode) tvars[0]);
			if (result != null) {
				env.setCurrent(result);
				return true;
			}
		}
		return false;
	}
	
	protected abstract IStrategoTerm call(IContext env, IWrappedAstNode node);
}
