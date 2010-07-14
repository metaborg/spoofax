package org.strategoxt.imp.metatooling.building;

import static org.eclipse.core.resources.IMarker.SEVERITY_ERROR;
import static org.strategoxt.imp.metatooling.loading.DynamicDescriptorLoader.getSourceDescriptor;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.imp.generator.sdf2imp;
import org.strategoxt.imp.generator.sdf2imp_jvm_0_0;
import org.strategoxt.imp.metatooling.loading.DynamicDescriptorLoader;
import org.strategoxt.imp.runtime.Debug;
import org.strategoxt.imp.runtime.Environment;
import org.strategoxt.imp.runtime.dynamicloading.DescriptorFactory;
import org.strategoxt.imp.runtime.parser.ast.AstMessageHandler;
import org.strategoxt.imp.runtime.stratego.EditorIOAgent;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.StrategoErrorExit;
import org.strategoxt.lang.StrategoExit;
import org.strategoxt.stratego_lib.dr_scope_all_end_0_0;
import org.strategoxt.stratego_lib.dr_scope_all_start_0_0;
import org.strategoxt.stratego_lib.stratego_lib;

/**
 * Runs the project generator on modified editor descriptors.
 * 
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class DynamicDescriptorBuilder {
	
	private static final DynamicDescriptorBuilder instance = new DynamicDescriptorBuilder();
	
	private final AstMessageHandler messageHandler =
		new AstMessageHandler(AstMessageHandler.ANALYSIS_MARKER_TYPE);
	
	private final Context context;
	
	private final EditorIOAgent agent;
	
	private boolean antBuildDisallowed;
	
	private DynamicDescriptorBuilder() {
		try {
			agent = new EditorIOAgent();
			context = new Context(Environment.getTermFactory(), agent);
			context.registerClassLoader(sdf2imp.class.getClassLoader());
			sdf2imp.init(context);
			assert sdf2imp._consSdfMainModuleFlag_0 != null && stratego_lib._consAlert_0 != null;
			
		} catch (Throwable e) { // (catch classes not loading, etc.)
			Environment.logException("Unable to initialize dynamic builder", e);
			throw new RuntimeException("Unable to initialize dynamic builder", e);
		}
	}
	
	public static DynamicDescriptorBuilder getInstance() {
		return instance;
	}
	
	public boolean isAntBuildDisallowed() {
		return antBuildDisallowed;
	}

	/**
	 * @return <code>true</code> if the resource was a descriptor and it was successfully reloaded.
	 */
	public boolean updateResource(IResource resource, IProgressMonitor monitor) {
		IPath location = resource.getRawLocation();
		if (location == null) return false;
		
		try {
			// System.err.println("Resource changed: " + resource.getName()); // DEBUG
			if (resource.exists() && isMainFile(resource)) {
				monitor.setTaskName("Building " + resource.getName());
				return buildDescriptor(resource, monitor);
			}
			
		} catch (RuntimeException e) {
			Environment.logException("Unable to build descriptor for " + resource, e);
			reportError(resource, "Unable to build descriptor for " + resource, e);
		} catch (Error e) { // workspace thread swallows this >:(
			reportError(resource, "Unable to build descriptor for " + resource, e);
			Environment.logException("Unable to build descriptor for " + resource, e);
		}
		return false;
	}

	/**
	 * Build and load a descriptor file.
	 * 
	 * @return <code>true</code> if the descriptor was successfully loaded.
	 */
	private boolean buildDescriptor(IResource mainFile, IProgressMonitor monitor) {
		try {
			Environment.assertLock();
			
			monitor.setTaskName("Notifying active editors");
			DescriptorFactory.prepareForReload(getSourceDescriptor(mainFile));
			try {
				prepareOutputFiles(mainFile);
			} catch (CoreException e) {
				Environment.logException("Could not prepare descriptor output files", e);
			}
			
			messageHandler.clearMarkers(mainFile);
			messageHandler.commitAllChanges();
			monitor.setTaskName("Generating service descriptors for " + mainFile.getName());
			IStrategoTerm result = invokeBuilder(mainFile);
			if (result == null) {
				String log = agent.getLog().trim();
				Environment.logException("Unable to build descriptor:\n" + log);
				messageHandler.addMarkerFirstLine(mainFile, "Unable to build descriptor (see error log)", SEVERITY_ERROR);
				// UNDONE: StrategoConsole.activateConsole(); (not good for ant triggered builds)
				return false;
			}
			
			monitor.setTaskName("Loading " + mainFile.getName());
			if (AntDescriptorBuilder.isActive())
				System.err.println("Loading new editor services");
			DynamicDescriptorLoader.getInstance().loadPackedDescriptor(getTargetDescriptor(mainFile));
			monitor.setTaskName(null);
			return true;
			
		} catch (IOException e) {
			Environment.logException("Unable to build descriptor for " + mainFile, e);
			messageHandler.addMarkerFirstLine(mainFile, "Internal error building descriptor (see error log)", SEVERITY_ERROR);
			return false;
		}
	}

	/**
	 * Invoke the Stratego-based descriptor builder.
	 * 
	 * @return  <code>true</code> if successful.
	 */
	private IStrategoTerm invokeBuilder(IResource mainFile) throws IOException {
		IPath location = mainFile.getRawLocation();
		String path = location.removeLastSegments(1).toOSString();
		String filename = mainFile.getName();
		IStrategoString input = context.getFactory().makeString(filename);
		
		agent.clearLog();
		agent.setWorkingDir(path);
		dr_scope_all_start_0_0.instance.invoke(context, input);
		
		try {
			Debug.startTimer();
			// UNDONE: setting the exceptionhandler has no effect,
			//         other than making eclipse not release the locks
			//context.getExceptionHandler().setEnabled(true);
			return sdf2imp_jvm_0_0.instance.invoke(context, input);
		} catch (StrategoErrorExit e) {
			context.printStackTrace();
			Environment.logException("Fatal error in dynamic builder, log:\n" + agent.getLog().trim(), e);
			reportError(mainFile, "Fatal error in descriptor " + mainFile + ": " + e.getMessage(), e);
			return null;
		} catch (StrategoExit e) {
			context.printStackTrace();
			Environment.logException("Unexpected exit in dynamic builder, log:\n" + agent.getLog().trim(), e);
			reportError(mainFile, "Unexpected exit in descriptor builder, log:" + agent.getLog().trim(), e);
			return null;
		} finally {
			//context.getExceptionHandler().setEnabled(false);
			Debug.stopTimer("Invoked descriptor builder for " + mainFile.getName());
			dr_scope_all_end_0_0.instance.invoke(context, input);
		}
	}
	
	private void reportError(final IResource descriptor, final String message, final Throwable exception) {
		Environment.assertLock();
		
		if (exception != null)
			Environment.asynOpenErrorDialog("Dynamic editor descriptor loading", message, exception);
		
		if (ResourcesPlugin.getWorkspace().isTreeLocked()) {
			Job job = new WorkspaceJob("Add error marker") {
				{ setSystem(true); } // don't show to user
				@Override
				public IStatus runInWorkspace(IProgressMonitor monitor) {
					messageHandler.addMarkerFirstLine(descriptor, message, SEVERITY_ERROR);
					return Status.OK_STATUS;
				}
			};
			job.setRule(descriptor);
			job.schedule();
		} else {
			messageHandler.addMarkerFirstLine(descriptor, message, SEVERITY_ERROR);
		}
	}
	
	private void prepareOutputFiles(IResource mainDescriptor) throws CoreException, IOException {
		IProject project = mainDescriptor.getProject();
		String name = mainDescriptor.getName();

		if (name.endsWith(".main.esv")) {
			name = name.substring(0, name.length() - ".main.esv".length());
		} else if (name.endsWith(".packed.esv")) {
			name = name.substring(0, name.length() - ".packed.esv".length());
		} else {
			throw new IOException("Main descriptor name must end with .main.esv: " + mainDescriptor);
		}		
		
		if (project.findMember("include/" + name + ".packed.esv") == null)
			refreshIncludeDir(project, name);
		
		setDerivedResources(project);
	}

	private void refreshIncludeDir(IProject project, String name) {
		DynamicDescriptorLoader.getInstance().forceNoUpdate(project.getFullPath() + "/include/" + name + ".packed.esv");
		IResource includeDir = project.findMember("/include");
		try {
			// Ant thread holds resource lock and wants environment; avoid deadlock
			// FIXME: ant thread deadlock can only be really avoided with a proper ReentrantLock environment lock
			if (includeDir == null) {
				antBuildDisallowed = true;
				Thread.sleep(10);
				project.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
			} else if (project.findMember("include/" + name + ".packed.esv") == null) {
				antBuildDisallowed = true;
				Thread.sleep(10);
				includeDir.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
			}
		} catch (CoreException e) {
			Environment.logException("Exception when refreshing resources", e);
		} catch (InterruptedException e) {
			Environment.logException("Exception when refreshing resources", e);
		} finally {
			antBuildDisallowed = false;
		}
	}

	@SuppressWarnings("deprecation") // use the Eclipse 3.5 and lower compatible API
	private static void setDerivedResources(IProject project) throws CoreException,
			IOException {
		
		IResource includeDir = project.findMember("include");
		IResource editorDir = project.findMember("editor");
		IResource syntaxDir = project.findMember("editor");
		// IResource buildFile = project.findMember("build.generated.xml");
		// IResource editorCommonFile = project.findMember("lib/editor-common.generated.str");
		IResource cacheDir = project.findMember(".cache");

		if (!(includeDir instanceof IContainer && editorDir instanceof IContainer))
			throw new IOException("/include and /editor directories must exist");
		
		if (!includeDir.isDerived()) {
			includeDir.setDerived(true);
			for (IResource member : ((IContainer) includeDir).members()) {
				member.setDerived(true);
			}			
			for (IResource member : ((IContainer) editorDir).members()) {
				if (member.getName().endsWith(".generated.esv"))
					member.setDerived(true);
			}
			//if (buildFile != null)
			//	buildFile.setDerived(true);
		}
		
		for (IResource member : ((IContainer) syntaxDir).members()) {
			if (member.getName().endsWith(".generated.pp"))
				member.setDerived(true);
		}
		
		if (cacheDir != null && cacheDir.exists()) cacheDir.setDerived(true);
		//if (editorCommonFile != null && editorCommonFile.exists()) editorCommonFile.setDerived(true);
	}
	
	public IResource getTargetDescriptor(IResource mainDescriptor) {
		String name = mainDescriptor.getName();
		if (name.endsWith(".packed.esv")) return mainDescriptor;
		name = name.substring(0, name.length() - ".main.esv".length());
		IProject project = mainDescriptor.getProject();
		refreshIncludeDir(project, name); // really ensure include/ exists
		IResource result = project.findMember("include/" + name + ".packed.esv");
		
		if (result == null) {
			Environment.logException("Target descriptor not found", new FileNotFoundException("include/" + name + ".packed.esv"));
			return mainDescriptor;
		} else {
			return result;
		}
	}

	private static boolean isMainFile(IResource file) {
		// TODO: Determine if a file is the main descriptor file by its contents?
		// InputStream stream = agent.openInputStream(file);
		
		if (file.getParent() != null && "bin".equals(file.getParent().getName()))
			return false;
		
		return file.toString().matches(".*(-Main|\\.main)\\.esv");
	}
}
