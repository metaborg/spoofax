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
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.imp.editor.UniversalEditor;
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
import org.strategoxt.imp.generator.sdf2imp;
import org.strategoxt.imp.metatooling.loading.DynamicDescriptorUpdater;
import org.strategoxt.imp.runtime.Environment;
import org.strategoxt.imp.runtime.stratego.EditorIOAgent;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.StrategoErrorExit;
import org.strategoxt.lang.StrategoException;
import org.strategoxt.lang.StrategoExit;
import org.strategoxt.permissivegrammars.make_permissive;

/**
 * A wizard for creating new Spoofax/IMP projects.
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
		final String languageName = input.getInputLanguageName();
		final String projectName = input.getInputProjectName();
		final String packageName = input.getInputPackageName();
		final String extensions = input.getInputExtensions();
		
		IRunnableWithProgress op = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException {
				try {
					doFinish(languageName, projectName, packageName, extensions, monitor);
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
			Environment.logException("Exception while creating new project", realException);
			MessageDialog.openError(getShell(), "Error: " + realException.getClass().getName(), realException.getMessage());
			return false;
		}
		return true;
	}
	
 	private void doFinish(String languageName, String projectName, String packageName, String extensions, IProgressMonitor monitor) throws IOException, CoreException {
		final int TASK_COUNT = 11;
		boolean success = false;
		monitor.beginTask("Creating " + languageName + " project", TASK_COUNT);
		
		monitor.setTaskName("Preparing project builder");
		EditorIOAgent agent = new EditorIOAgent();		
		Context context = new Context(Environment.getTermFactory(), agent);
		sdf2imp.init(context);
		monitor.worked(1);

		monitor.setTaskName("Creating project");
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IProject project = workspace.getRoot().getProject(projectName);
		project.create(null);
		project.open(null);
		// UNDONE: deleting the .project file doesn't always work, likely due to threading
		// project.findMember(".project").delete(true, new NullProgressMonitor());
		monitor.worked(1);

		try {
			agent.setWorkingDir(project.getLocation().toOSString());
			try {
				String jar1 = org.strategoxt.stratego_lib.Main.class.getProtectionDomain().getCodeSource().getLocation().getFile();
				String jar2 = make_permissive.class.getProtectionDomain().getCodeSource().getLocation().getFile();
				String jar3 = sdf2imp.class.getProtectionDomain().getCodeSource().getLocation().getFile();
				assert jar1.endsWith(".jar") && jar2.endsWith(".jar") && jar3.endsWith(".jar");
				sdf2imp.mainNoExit(context, "-m", languageName, "-pn", projectName, "-n", packageName, "-e", extensions, "-jar", jar1, jar2, jar3);
			} catch (StrategoErrorExit e) {
				Environment.logException(e);
				throw new StrategoException("Project builder failed: " + e.getMessage(), e);
			} catch (StrategoExit e) {
				if (e.getValue() != 0) {
					throw new StrategoException("Project builder failed. Log follows\n\n"
							+ agent.getLog(), e);
				}
			}
			monitor.worked(3);

			monitor.setTaskName("Acquiring project lock");
			Job.getJobManager().beginRule(project, monitor);  // avoid ant builder launching
			try {
				monitor.setTaskName("Acquiring environment lock");
				monitor.worked(1);
				synchronized (Environment.getSyncRoot()) { // avoid background editor loading
					monitor.setTaskName("Loading new resources");
					project.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
					monitor.worked(1);
					
					monitor.setTaskName("Loading the new editor");
					IResource descriptor = project.findMember("include/" + languageName + ".packed.esv");
					DynamicDescriptorUpdater.getInstance().forceUpdate(descriptor);
				}
				monitor.worked(1);
			} finally {
				Job.getJobManager().endRule(project);
			}
			
			monitor.setTaskName("Opening files for editing");
			openEditor(project, "/editor/" + languageName +  ".main.esv", true);
			monitor.worked(1);
			openEditor(project, "/syntax/" + languageName +  ".sdf", true);
			monitor.worked(1);
			openEditor(project, "/test/example." + extensions.split(",")[0], false);
			monitor.worked(1);
			openEditor(project, "/syntax/" + languageName +  ".sdf", true); // honestly, give this one focus
			monitor.done();
			
			success = true;
			
		} finally {
			if (!success) {
				monitor.setTaskName("Undoing workspace operations");
				project.delete(true, null);
			}
		}
	}
	
	private void openEditor(IProject project, String filename, final boolean activate) {
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
					IDE.openEditor(page, (IFile) file, UniversalEditor.EDITOR_ID, activate);
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