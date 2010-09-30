package org.strategoxt.imp.runtime.stratego;

import static org.eclipse.core.resources.IResource.DEPTH_INFINITE;
import static org.spoofax.interpreter.core.Tools.asJavaString;
import static org.spoofax.interpreter.core.Tools.isTermString;

import java.io.FileNotFoundException;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.core.InterpreterException;
import org.spoofax.interpreter.library.AbstractPrimitive;
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
			resource = EditorIOAgent.getResource(env, file);
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

}
