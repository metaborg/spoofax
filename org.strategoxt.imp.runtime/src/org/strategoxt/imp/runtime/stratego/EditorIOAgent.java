package org.strategoxt.imp.runtime.stratego;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.net.URI;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.library.IOAgent;
import org.spoofax.interpreter.library.LoggingIOAgent;
import org.spoofax.interpreter.library.ssl.SSLLibrary;
import org.strategoxt.imp.runtime.Debug;
import org.strategoxt.imp.runtime.dynamicloading.Descriptor;
import org.strategoxt.imp.runtime.services.StrategoAnalysisJob;

/**
 * This class overrides the default IOAgent to support attached files in editor plugins,
 * and may redirect any disk reads to the Eclipse API. 
 * 
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class EditorIOAgent extends LoggingIOAgent {
	
	private Descriptor descriptor;
	
	private boolean alwaysActivateConsole;

	private String projectPath;
	
	private IProject project;
	
	private StrategoAnalysisJob job;
	
	public void setDescriptor(Descriptor descriptor) {
		this.descriptor = descriptor;
	}
	
	public Descriptor getDescriptor() {
		return descriptor;
	}
	
	@Override
	public InputStream openInputStream(String path, boolean isDefinitionFile)
			throws FileNotFoundException {
		
		if (isDefinitionFile && descriptor != null) {
			return openAttachedFile(path);
		} else {
			return super.openInputStream(path, isDefinitionFile);
		}
	}
	
	private InputStream openAttachedFile(String path) throws FileNotFoundException {
		try {
			return descriptor.openAttachment(path);
		} catch (FileNotFoundException e) {
			File localFile = new File(path);
			if (localFile.exists()) {
				Debug.log("Reading file form the current directory: ", path);  
				return new BufferedInputStream(new FileInputStream(localFile));
			} else {
				throw e;
			}
		}
	}
	
	@Override
	public Writer getWriter(int fd) {
		// TODO: close console streams after use?
		// TODO: show console during wizard
		//       (same goes for internalGetOutputStream)
		if (fd == CONST_STDOUT && isActivateConsoleEnabled()) {
			StrategoConsole.activateConsole(true);
			return StrategoConsole.getOutputWriter();
		} else if (fd == CONST_STDERR && isActivateConsoleEnabled()) {
			StrategoConsole.activateConsole(true);
			return StrategoConsole.getErrorWriter();
		} else {
			return super.getWriter(fd);
		}
	}
	
	@Override
	public OutputStream internalGetOutputStream(int fd) {
		if (fd == CONST_STDOUT && isActivateConsoleEnabled()) {
			StrategoConsole.activateConsole(true);
			return StrategoConsole.getOutputStream();
		} else if (fd == CONST_STDERR && isActivateConsoleEnabled()) {
			StrategoConsole.activateConsole(true);
			return StrategoConsole.getErrorStream();
		} else {
			return super.internalGetOutputStream(fd);
		}
	}

	private boolean isActivateConsoleEnabled() {
		return alwaysActivateConsole || (descriptor != null && descriptor.isDynamicallyLoaded());
	}
	
	public void setAlwaysActivateConsole(boolean alwaysShowConsole) {
		this.alwaysActivateConsole = alwaysShowConsole;
	}

	/**
	 * Sets the path of the current project, if available.
	 * Should be an OS path for compatibility.
	 */
	public void setProjectPath(String projectPath) {
		this.projectPath = projectPath;
	}
	
	public String getProjectPath() {
		return projectPath;
	}
	
	public void setJob(StrategoAnalysisJob job) {
		this.job = job;
	}
	
	public StrategoAnalysisJob getJob() {
		return this.job;
	}
	
	/**
	 * Gets the IProject for this agent.
	 */
	public IProject getProject() {
		return project;
	}
	
	public void setProject(IProject project) {
		this.project = project;
	}

	public static IFile getFile(IContext env, String file) throws FileNotFoundException {
		IResource res = getResource(env, file);
		if (res.getType() == IResource.FILE) {
			return (IFile)res;
		}
		throw new FileNotFoundException("Resource is not a file: " + file);
	}
	
	public static IResource getResource(IContext env, String file) throws FileNotFoundException {
		IOAgent agent = SSLLibrary.instance(env).getIOAgent();
		File file2 = new File(file);
		if (!file2.exists() && !file2.isAbsolute())
			file2 = new File(agent.getWorkingDir() + "/" + file);
		return getResource(file2);
	}

	public static IResource getResource(File file) throws FileNotFoundException {
		if (file == null) {
			assert false : "file should not be null";
			return null;
		}
		URI uri = file.toURI();
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IResource[] resources = workspace.getRoot().findContainersForLocationURI(uri);
		if (resources.length > 0 && !resources[0].exists()) // prefer files over dirs
			resources = workspace.getRoot().findFilesForLocationURI(uri); 
		if (resources.length == 0)
			throw new FileNotFoundException("File not in workspace: " + file);
		assert resources.length == 1;
		return resources[0];
	}
	
	public static boolean isResource(File file){
		URI uri = file.toURI();
		IWorkspace workspace; 
	    try {
	    	workspace = ResourcesPlugin.getWorkspace();
	    } catch (IllegalStateException e) {
	    	// there is no workspace, i.e. not running as a plug-in
	    	return false;
	    }
		IResource[] resources = workspace.getRoot().findContainersForLocationURI(uri);
		return resources.length > 0;
	}
}
