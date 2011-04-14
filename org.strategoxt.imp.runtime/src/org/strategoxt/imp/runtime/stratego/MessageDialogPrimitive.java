package org.strategoxt.imp.runtime.stratego;

import static org.spoofax.interpreter.core.Tools.isTermString;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.progress.UIJob;
import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.core.InterpreterException;
import org.spoofax.interpreter.library.AbstractPrimitive;
import org.spoofax.interpreter.stratego.Strategy;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.imp.runtime.Environment;

/**
 * @author Maartje de Jonge
 */
public class MessageDialogPrimitive extends AbstractPrimitive {

	public MessageDialogPrimitive() {
		super("SSL_EXT_openmessagedialog", 0, 3); 
	}
	
	@Override
	public boolean call(final IContext env, Strategy[] svars, IStrategoTerm[] tvars)
			throws InterpreterException {
		
		if (!isTermString(tvars[0])) return false;
		if (!isTermString(tvars[1])) return false;
		if (!isTermString(tvars[2])) return false;
		
		final String title = ((IStrategoString)tvars[0]).stringValue(); 
		final String message = ((IStrategoString)tvars[1]).stringValue();
		final String type = ((IStrategoString)tvars[2]).stringValue();
		final  Boolean[] dialogResultOk={false};

		Job job = new UIJob("user input") {
			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				MessageDialog dialog;
				if (type.equals("WARNING"))
					dialog = new MessageDialog(null, title, null, message, MessageDialog.WARNING, new String[]{IDialogConstants.OK_LABEL, IDialogConstants.CANCEL_LABEL}, 1);
				else if (type.equals("ERROR"))
					dialog = new MessageDialog(null, title, null, message, MessageDialog.ERROR, new String[]{IDialogConstants.OK_LABEL}, 0);
				else if (type.equals("INFO"))
					dialog = new MessageDialog(null, title, null, message, MessageDialog.INFORMATION, new String[]{IDialogConstants.OK_LABEL}, 0);
				else
					dialog = new MessageDialog(null, title, null, message, MessageDialog.NONE, new String[]{IDialogConstants.OK_LABEL, IDialogConstants.CANCEL_LABEL}, 0);					

				if (dialog.open() == 0) {
					dialogResultOk[0]=true;
				} 
				return Status.OK_STATUS;
			}
		};
		job.setSystem(true);
		job.schedule();	
		try {
			job.join();
		} catch (InterruptedException e) {
			Environment.logException("Interrupted", e);
		}
		return job.getResult()==Status.OK_STATUS && dialogResultOk[0]==true;
	}
}
