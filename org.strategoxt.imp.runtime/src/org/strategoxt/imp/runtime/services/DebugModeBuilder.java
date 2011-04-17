package org.strategoxt.imp.runtime.services;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.ui.progress.UIJob;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.imp.runtime.EditorState;
import org.strategoxt.imp.runtime.Environment;
import org.strategoxt.imp.runtime.MonitorStateWatchDog;
import org.strategoxt.imp.runtime.RuntimeActivator;

/**
 * Toggles between stratego debugging.
 * 
 * If debug mode is allowed in Environment.allowsDebugging(Descriptor) the user can disable or enable debugging.
 * @author rlindeman
 *
 */
public class DebugModeBuilder implements IBuilder {

	private final StrategoObserver observer;
	
	public DebugModeBuilder(StrategoObserver observer) {
		this.observer = observer;
	}

	public String getCaption() {
		if (this.observer.isDebuggerEnabled())
		{
			return "Disable debug mode";
		}
		else
		{
			return "Enable debug mode";
		}
	}

	public Job scheduleExecute(final EditorState editor, IStrategoTerm node,
			final IFile errorReportFile, final boolean isRebuild) {
		// ignore the parameters, we just want to toggle the debug mode and rebuild the project (if necessary)

		boolean isDebuggerEnabled = observer.isDebuggerEnabled();
		observer.setDebuggerEnabled(!isDebuggerEnabled); // toggle
		boolean needsProjectRebuild = false;
		if (observer.isDebuggerEnabled())
		{
			try {
				needsProjectRebuild = observer.needsProjectRebuild();
			} catch (CoreException e) {
				this.openError(editor, e.getMessage());
			}
		}
		System.out.println("NEEDS REBUILD: " + needsProjectRebuild);
		Job job = null;
		if (needsProjectRebuild)
		{
			job = new Job("Executing " + displayCaption) {
				@Override
				protected IStatus run(IProgressMonitor monitor) {
					MonitorStateWatchDog protector = new MonitorStateWatchDog(this, monitor, observer);
					try {
						execute(editor, monitor);
						return monitor.isCanceled() ? Status.CANCEL_STATUS : Status.OK_STATUS;
					} finally {
						protector.endProtect();
					}
				}
			};
			job.setUser(true);
			job.schedule();
		}
		return job;
	}
	
	/**
	 * Executes a rebuild of the Project.
	 * @param editor
	 * @param monitor
	 */
	private void execute(EditorState editor, IProgressMonitor monitor) {
		// rebuild the project
		IProject project = this.observer.getProject();
		if (project != null)
		{
			int kind = IncrementalProjectBuilder.INCREMENTAL_BUILD;
			//int kind = IncrementalProjectBuilder.FULL_BUILD;
			
			//IProgressMonitor monitor = null;
			try {
				project.build(kind, monitor);
			} catch (CoreException e) {
				Environment.logException(e.getMessage());
				openError(editor, e.getMessage());
			}
		}
	}

	public Object getData() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setData(Object data) {
		// TODO Auto-generated method stub
		
	}

	private String displayCaption = "Debug mode";
	
	private void openError(EditorState editor, String message) {
		try {
			Status status = new Status(IStatus.ERROR, RuntimeActivator.PLUGIN_ID, message);
			ErrorDialog.openError(editor.getEditor().getSite().getShell(),
					displayCaption, null, status);
		} catch (RuntimeException e) {
			Environment.logException("Problem reporting error: " + message, e);
		}
	}
}
