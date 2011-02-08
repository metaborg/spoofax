package org.strategoxt.imp.runtime.stratego;

import static org.spoofax.interpreter.core.Tools.isTermAppl;
import static org.spoofax.interpreter.core.Tools.isTermInt;
import static org.spoofax.interpreter.core.Tools.isTermList;
import static org.spoofax.interpreter.core.Tools.javaInt;

import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.core.InterpreterException;
import org.spoofax.interpreter.library.AbstractPrimitive;
import org.spoofax.interpreter.stratego.Strategy;
import org.spoofax.interpreter.terms.IStrategoTerm;

/**
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class SubtermPrimitive extends AbstractPrimitive {

	public SubtermPrimitive() {
		super("SSL_EXT_subterm", 0, 2);
	}
	
	@Override
	public boolean call(IContext env, Strategy[] svars, IStrategoTerm[] tvars)
			throws InterpreterException {
		
		if (!isTermAppl(tvars[0]) && !isTermList(tvars[0])) return false;
		if (!isTermInt(tvars[1])) return false;
		
		IStrategoTerm input = tvars[0];
		int index = javaInt(tvars[1]);
		
		if (0 > index || index > input.getSubtermCount()) return false;
		
		env.setCurrent(input.getSubterm(index));
		
		return true;
	}

}
