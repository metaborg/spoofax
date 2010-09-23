package org.strategoxt.imp.runtime.stratego;

import static org.eclipse.core.resources.IResource.DEPTH_INFINITE;
import static org.spoofax.interpreter.core.Tools.asJavaString;
import static org.spoofax.interpreter.core.Tools.isTermString;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URI;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.core.InterpreterException;
import org.spoofax.interpreter.library.AbstractPrimitive;
import org.spoofax.interpreter.library.IOAgent;
import org.spoofax.interpreter.library.ssl.SSLLibrary;
import org.spoofax.interpreter.stratego.Strategy;
import org.spoofax.interpreter.terms.IStrategoTerm;

/**
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class RefreshResourcePrimitive extends AbstractPrimitive {

	public RefreshResourcePrimitive() {
		super("SSL_EXT_refreshresource", 0, 1);
	}

	@Override
	public boolean call(IContext env, Strategy[] svars, IStrategoTerm[] tvars)
			throws InterpreterException {
		
		if (!isTermString(tvars[0]))
			return false;
		
		return call(env, asJavaString(tvars[0]));
	}

	public static boolean call(IContext env, String file) {
		final IResource resource;
		try {
			resource = getResource(env, file);
		} catch (FileNotFoundException e) {
			return false;
		}
		
		// Cannot acquire a workspace lock here:
		// the Ant thread acquires 1) a workspace lock and 2) the environment lock
		// we cannot go against that order here or would risk a deadlock
		
		Job job = new Job("Refreshing resource") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				try {
					resource.refreshLocal(DEPTH_INFINITE, new NullProgressMonitor());
				} catch (CoreException e) {
					// Ignore
				} catch (RuntimeException e) {
					// Ignore
				}
				return Status.OK_STATUS;
			}
		};
		job.setSystem(true);
		job.schedule(0);
		return true;
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
		if (resources.length == 0)
			throw new FileNotFoundException("File not in workspace: " + file);
		return resources[0];
	}

}
