package org.strategoxt.imp.runtime.stratego;

import static org.spoofax.interpreter.core.Tools.isTermString;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.UIJob;
import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.core.InterpreterException;
import org.spoofax.interpreter.library.AbstractPrimitive;
import org.spoofax.interpreter.stratego.Strategy;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.imp.runtime.Environment;

/**
 * @author Maartje de Jonge
 */
public class SaveAllResourcesPrimitive extends AbstractPrimitive {

	public SaveAllResourcesPrimitive() {
		super("SSL_EXT_saveresources", 0, 1);
	}

	@Override
	public boolean call(IContext env, Strategy[] svars, IStrategoTerm[] tvars)
			throws InterpreterException {		
		if (!isTermString(tvars[0]))
			return false;
		Job job = new UIJob("Save editors") {
			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				IWorkbenchWindow activeWorkbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
				if (activeWorkbenchWindow == null)
					throw new IllegalStateException("Must be called from UI thread");
				PlatformUI.getWorkbench().saveAllEditors(false);
				return Status.OK_STATUS;
			}
		};
		job.setSystem(true);
		job.schedule();
		try {
			job.join();
		} catch (InterruptedException e) {
			Environment.logException(e);
		}
		return job.getResult()==Status.OK_STATUS;
	}
}
