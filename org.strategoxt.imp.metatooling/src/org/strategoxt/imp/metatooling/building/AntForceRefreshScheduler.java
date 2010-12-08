package org.strategoxt.imp.metatooling.building;

import java.io.File;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.strategoxt.imp.runtime.Environment;
import org.strategoxt.imp.runtime.stratego.EditorIOAgent;

/**
 * Helper class to force the Eclipse Ant builder to
 * rebuild a project even if it didn't change (Spoofax/311).
 * 
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class AntForceRefreshScheduler {
	public static void main(String[] args) throws Exception {
		if (args == null || args.length == 0)
			throw new IllegalArgumentException("File expected");
		
		final String fileArg = args[0];
		final int depthArg = args.length >= 2 ? Integer.parseInt(args[1]) : IResource.DEPTH_INFINITE;
		final IResource file = EditorIOAgent.getResource(new File(fileArg));
		
		Job job = new Job("Refresh") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				try {
					file.touch(monitor);
					file.refreshLocal(depthArg, monitor);
				} catch (Exception e) {
					Environment.logWarning("Could not refresh file", e);
				}
				return Status.OK_STATUS;
			}
		};
		job.setSystem(true);
		job.schedule(500);
	}
}
