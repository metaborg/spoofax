package org.metaborg.spoofax.core.stratego.primitive.generic;

import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.core.InterpreterException;
import org.spoofax.interpreter.library.AbstractPrimitive;
import org.spoofax.interpreter.stratego.Strategy;
import org.spoofax.interpreter.terms.IStrategoTerm;

public class RedirectingPrimitive extends AbstractPrimitive {
	private final AbstractPrimitive redirectTo;

	
	public RedirectingPrimitive(String name, AbstractPrimitive redirectTo) {
		super(name, redirectTo.getSArity(), redirectTo.getTArity());
		this.redirectTo = redirectTo;
	}

	
	@Override
	public boolean call(IContext env, Strategy[] strategies, IStrategoTerm[] terms) throws InterpreterException {
		return redirectTo.call(env, strategies, terms);
	}
}
