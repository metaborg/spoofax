package org.strategoxt.imp.runtime.stratego;

import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.core.InterpreterException;
import org.spoofax.interpreter.library.AbstractPrimitive;
import org.spoofax.interpreter.stratego.Strategy;
import org.spoofax.interpreter.terms.IStrategoTerm;

/**
 * @author Maartje de Jonge
 */
public class TermPathPrimitive extends AbstractPrimitive {

	public TermPathPrimitive() {
		super("SSL_EXT_term_path", 0, 1);
	}
	
	@Override
	public boolean call(IContext env, Strategy[] svars, IStrategoTerm[] tvars)
			throws InterpreterException {
		
		if (tvars.length != 1) return false;
		env.setCurrent(StrategoTermPath.createPath(tvars[0]));		
		return true;
	}

}
