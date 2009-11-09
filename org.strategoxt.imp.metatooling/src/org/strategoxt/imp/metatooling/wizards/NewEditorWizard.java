package org.strategoxt.imp.metatooling.wizards;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.spoofax.interpreter.core.Interpreter;
import org.strategoxt.imp.generator.sdf2imp;
import org.strategoxt.imp.metatooling.loading.DynamicDescriptorUpdater;
import org.strategoxt.imp.runtime.EditorState;
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
	
	private IProject lastProject;

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
					throw new InvocationTargetException(e);
				} finally {
					monitor.done();
				}
			}
		};
		
		try {
			getContainer().run(true, false, op);
		} catch (InterruptedException e) {
			rollback();
			return false;
		} catch (InvocationTargetException e) {
			Throwable realException = e.getTargetException();
			Environment.logException("Exception while creating new project", realException);
			MessageDialog.openError(getShell(), "Error: " + realException.getClass().getName(), realException.getMessage());
			rollback();
			return false;
		}
		return true;
	}
	
	private void rollback() {
		// monitor.setTaskName("Undoing workspace operations");
		try {
			if (lastProject != null) lastProject.delete(true, null);
		} catch (CoreException e) {
			Environment.logException("Could not remove new project", e);
		}
	}
	
 	private void doFinish(String languageName, String projectName, String packageName, String extensions, IProgressMonitor monitor) throws IOException, CoreException {
		final int TASK_COUNT = 17;
		lastProject = null;
		monitor.beginTask("Creating " + languageName + " project", TASK_COUNT);
		
		monitor.setTaskName("Preparing project builder");
		EditorIOAgent agent = new EditorIOAgent();		
		Context context = new Context(Environment.getTermFactory(), agent);
		context.registerClassLoader(make_permissive.class.getClassLoader());
		sdf2imp.init(context);
		monitor.worked(1);

		monitor.setTaskName("Creating project");
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IProject project = lastProject = workspace.getRoot().getProject(projectName);
		project.create(null);
		project.open(null);
		monitor.worked(1);

		agent.setWorkingDir(project.getLocation().toOSString());
		try {
			String jar1 = org.strategoxt.stratego_lib.Main.class.getProtectionDomain().getCodeSource().getLocation().getFile();
			String jar2 = make_permissive.class.getProtectionDomain().getCodeSource().getLocation().getFile();
			String jar3 = sdf2imp.class.getProtectionDomain().getCodeSource().getLocation().getFile();
			if (!jar1.endsWith(".jar")) { // ensure correct jar at development time
				String jar1a = jar1 + "/../strategoxt.jar";
				if (new File(jar1a).exists()) jar1 = jar1a;
			}
			assert jar1.endsWith(".jar") && jar2.endsWith(".jar") && jar3.endsWith(".jar") : "Library files are not in JAR"; // please refresh the strj projectin Eclipse
			sdf2imp.mainNoExit(context, "-m", languageName, "-pn", projectName, "-n", packageName, "-e", extensions, "-jar", jar1, jar2, jar3);
		} catch (StrategoErrorExit e) {
			Environment.logException(e);
			throw new StrategoException("Project builder failed: " + e.getMessage() + "\nLog follows:\n\n"
					+ agent.getLog(), e);
		} catch (StrategoExit e) {
			if (e.getValue() != 0) {
				throw new StrategoException("Project builder failed.\nLog follows:\n\n"
						+ agent.getLog(), e);
			}
		}
		monitor.worked(3);

		monitor.setTaskName("Acquiring workspace lock"); // need root lock for builder
		IWorkspaceRoot root = project.getWorkspace().getRoot();
		Job.getJobManager().beginRule(root, monitor); // avoid ant builder launching
		try {
			monitor.setTaskName("Acquiring environment lock");
			monitor.worked(1);
			synchronized (Environment.getSyncRoot()) { // avoid background editor loading
				monitor.setTaskName("Loading new resources");
				project.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
				monitor.worked(1);
				
				monitor.setTaskName("Building example editor");
				project.build(IncrementalProjectBuilder.FULL_BUILD, null);
				monitor.worked(4);

				monitor.setTaskName("Loading editor");
				IResource descriptor = project.findMember("include/" + languageName + ".packed.esv");
				DynamicDescriptorUpdater.getInstance().forceUpdate(descriptor);
				monitor.worked(2);

				project.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
				monitor.worked(1);

			}
		} finally {
			Job.getJobManager().endRule(root);
		}

		monitor.setTaskName("Opening editor tabs");
		Display display = getShell().getDisplay();
		EditorState.asyncOpenEditor(display,  project, "/trans/" + toStrategoName(languageName) +  ".str", true);
		monitor.worked(1);
		EditorState.asyncOpenEditor(display, project, "/editor/" + languageName +  ".main.esv", true);
		monitor.worked(1);
		EditorState.asyncOpenEditor(display, project, "/syntax/" + languageName +  ".sdf", true);
		monitor.worked(1);
		EditorState.asyncOpenEditor(display, project, "/test/example." + extensions.split(",")[0], false);
		monitor.done();
	}
 	
 	private static String toStrategoName(String languageName) {
 		return Interpreter.cify(languageName.toLowerCase()).replace('-', '_');
 	}

	/*
	private void throwCoreException(String message) throws CoreException {
		IStatus status =
			new Status(IStatus.ERROR, "org.strategoxt.imp.metatooling", IStatus.OK, message, null);
		throw new CoreException(status);
	}
	*/
}