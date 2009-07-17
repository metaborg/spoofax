package org.strategoxt.imp.metatooling.wizards;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.spoofax.interpreter.core.Interpreter;
import org.spoofax.interpreter.core.InterpreterException;
import org.spoofax.interpreter.core.InterpreterExit;
import org.strategoxt.imp.editors.editorservice.EditorServiceParseController;
import org.strategoxt.imp.metatooling.MetatoolingActivator;
import org.strategoxt.imp.runtime.Environment;
import org.strategoxt.imp.runtime.stratego.EditorIOAgent;
import org.strategoxt.imp.runtime.stratego.adapter.WrappedAstNodeFactory;

/**
 * 
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class NewEditorWizard extends Wizard implements INewWizard {
	private final NewEditorWizardPage input = new NewEditorWizardPage();

	// TODO: Support external directory and working set selection in wizard
			
	public NewEditorWizard() {
		setNeedsProgressMonitor(true);
	}

	public void init(IWorkbench workbench, IStructuredSelection selection) {
		// No further initialization required
	}
	
	@Override
	public void addPages() {
		addPage(input);
	}

	/**
	 * This method is called when 'Finish' button is pressed in
	 * the wizard. We will create an operation and run it
	 * using wizard as execution context.
	 */
	@Override
	public boolean performFinish() {
		final String name = input.getInputName();
		final String packageName = input.getInputPackageName();
		final String extensions = input.getInputExtensions();
		
		IRunnableWithProgress op = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException {
				try {
					doFinish(name, packageName, extensions, monitor);
				} catch (Exception e) {
					Environment.logException("Error creating new project", e);
					throw new InvocationTargetException(e);
				} finally {
					monitor.done();
				}
			}
		};
		
		try {
			getContainer().run(true, false, op);
		} catch (InterruptedException e) {
			return false;
		} catch (InvocationTargetException e) {
			Throwable realException = e.getTargetException();
			MessageDialog.openError(getShell(), "Error", realException.getMessage());
			return false;
		}
		return true;
	}
	
	/**
	 * The worker method. It will find the container, create the
	 * file if missing or just replace its contents, and open
	 * the editor on the newly created file.
	 */

	private void doFinish(String name, String packageName, String extensions, IProgressMonitor monitor) throws InterpreterException, IOException, CoreException {
		final int TASK_COUNT = 5;
		monitor.beginTask("Creating " + name + " project", TASK_COUNT);
		
		monitor.setTaskName("Preparing project builder...");
		Interpreter builder = Environment.createInterpreter();
		Environment.addToInterpreter(builder, MetatoolingActivator.getResourceAsStream("/include/sdf2imp.ctree"));
		EditorIOAgent agent = new EditorIOAgent();		
		agent.setDescriptor(EditorServiceParseController.getDescriptor());
		builder.setIOAgent(agent);
		monitor.worked(1);

		monitor.setTaskName("Creating project...");
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IProject project = workspace.getRoot().getProject(name);
		project.create(null);
		project.open(null);
		try {
			WrappedAstNodeFactory terms = Environment.getTermFactory();
			builder.setCurrent(terms.makeList(
				terms.makeString("sdf2imp"),
				terms.makeString("-m"), terms.makeString(name),
				terms.makeString("-n"), terms.makeString(packageName),
				terms.makeString("-e"), terms.makeString(extensions)
			));
			agent.setWorkingDir(project.getLocation().toOSString());
			synchronized (Environment.getSyncRoot()) {
				try {
					builder.invoke("main-sdf2imp");
				} catch (InterpreterExit e) {
					if (e.getValue() != 0) {
						throw new InterpreterException("Project builder failed. Log follows\n\n"
								+ agent.getLog());
					}
				}
			}
			project.refreshLocal(IResource.DEPTH_INFINITE, monitor);
			monitor.worked(1);		
			
			monitor.setTaskName("Opening files for editing...");
			openEditor(project, "/syntax/" + name +  ".sdf");
			openEditor(project, "/editor/" + name +  ".main.esv");
			monitor.worked(1);
		} finally {
			monitor.setTaskName("Undoing workspace operations...");
			project.delete(true, null);
		}
	}
	
	private void openEditor(IProject project, String filename) {
		final IResource file = (IResource) project.findMember(filename);
		if (!file.exists() || !(file instanceof IFile)) {
			Environment.logException("Cannot open an editor for " + filename);
			return;
		}
		getShell().getDisplay().asyncExec(new Runnable() {
			public void run() {
				IWorkbenchPage page =
					PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
				try {
					IDE.openEditor(page, (IFile) file, true);
				} catch (PartInitException e) {
					Environment.logException("Cannot open an editor for " + file, e);
				}
			}
		});
	}

	/*
	private void throwCoreException(String message) throws CoreException {
		IStatus status =
			new Status(IStatus.ERROR, "org.strategoxt.imp.metatooling", IStatus.OK, message, null);
		throw new CoreException(status);
	}
	*/
}