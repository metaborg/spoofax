package org.strategoxt.imp.runtime.stratego;

import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.core.InterpreterException;
import org.spoofax.interpreter.library.AbstractPrimitive;
import org.spoofax.interpreter.library.IOAgent;
import org.spoofax.interpreter.library.ssl.SSLLibrary;
import org.spoofax.interpreter.stratego.Strategy;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.StrategoInt;
import org.strategoxt.imp.runtime.services.StrategoAnalysisJob;
import org.strategoxt.imp.runtime.services.StrategoProgressMonitor;

public class SetTotalWorkUnitsPrimitive extends AbstractPrimitive {

	public SetTotalWorkUnitsPrimitive() {
		super("SSL_EXT_set_total_work_units", 0, 0);
	}
	
	protected static StrategoProgressMonitor getProgress(IContext env) {
		IOAgent agent = SSLLibrary.instance(env).getIOAgent();
		if (!(agent instanceof EditorIOAgent)) return null;
			
		StrategoAnalysisJob job = ((EditorIOAgent)agent).getJob();
		if (job==null) return null;
		
		StrategoProgressMonitor progress = job.getProgressMonitor();
		return progress;
	}

	@Override
	public boolean call(IContext env, Strategy[] svars, IStrategoTerm[] tvars)
			throws InterpreterException {
		
		if (!(env.current() instanceof StrategoInt)) return false;
		StrategoProgressMonitor progress = getProgress(env);
		if (progress == null) return false;
		
		StrategoInt workUnits = (StrategoInt)env.current();
		progress.setSubTasks(workUnits.intValue());
		
		return true;
	}

}
