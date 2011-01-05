package org.strategoxt.imp.runtime.stratego;

import static org.spoofax.interpreter.core.Tools.isTermList;
import static org.spoofax.interpreter.core.Tools.isTermString;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.ui.progress.UIJob;
import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.core.InterpreterException;
import org.spoofax.interpreter.library.AbstractPrimitive;
import org.spoofax.interpreter.stratego.Strategy;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;

/**
 * @author Maartje de Jonge
 */
public class DialogPrimitive extends AbstractPrimitive {

	public DialogPrimitive() {
		super("SSL_EXT_opendialog", 0, 3); 
	}
	
	@Override
	public boolean call(final IContext env, Strategy[] svars, IStrategoTerm[] tvars)
			throws InterpreterException {
		
		if (!isTermString(tvars[0]) && !isTermList(tvars[0])) return false;
		if (!isTermString(tvars[1])) return false;
		if (!isTermString(tvars[2])) return false;
		
		final String title = ((IStrategoString)tvars[0]).stringValue(); 
		final String message = ((IStrategoString)tvars[1]).stringValue();
		final String input = ((IStrategoString)tvars[2]).stringValue();
		
		Job job = new UIJob("user input") {
			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				InputDialog dialog = new InputDialog(null, title, message, input, null);
				if (dialog.open() == InputDialog.OK) {
					String userInput=dialog.getValue();
					env.setCurrent(env.getFactory().makeString(userInput));
				} 
				return Status.OK_STATUS;
			}
		};
		job.setSystem(true);
		job.schedule();	
		try {
			job.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return job.getResult()==Status.OK_STATUS;
	}
}
