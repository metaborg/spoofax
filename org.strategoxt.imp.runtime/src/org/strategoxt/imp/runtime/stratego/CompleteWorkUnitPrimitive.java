package org.strategoxt.imp.runtime.stratego;

import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.core.InterpreterException;
import org.spoofax.interpreter.library.AbstractPrimitive;
import org.spoofax.interpreter.stratego.Strategy;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.imp.runtime.services.StrategoProgressMonitor;

public class CompleteWorkUnitPrimitive extends AbstractPrimitive {

	public CompleteWorkUnitPrimitive() {
		super("SSL_EXT_complete_work_unit", 0, 0);
	}
	
	@Override
	public boolean call(IContext env, Strategy[] svars, IStrategoTerm[] tvars)
			throws InterpreterException {
		
		StrategoProgressMonitor progress = SetTotalWorkUnitsPrimitive.getProgress(env);
		if (progress == null) return false;
		
		progress.completeTask();
		
		return true;
	}

}
