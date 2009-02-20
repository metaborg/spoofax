package org.strategoxt.imp.runtime;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;

/**
 * A utility class for running jobs atomically in the workspace thread,
 * avoiding workspace locks.
 * 
 * (This class avoids an Eclipse API problem where no workspace jobs
 * can be launched when the workspace is currently locked, e.g.,
 * when attempting to do so from a resource listener.
 * Essentially, this is a big, dirty hack.)
 * 
 * See:
 *   http://www.eclipse.org/articles/Article-Resource-deltas/resource-deltas.html
 *   http://dev.eclipse.org/newslists/news.eclipse.platform/msg75598.html
 *  
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class WorkspaceRunner {
	
	private static final ExecutorService threadLauncher = Executors.newSingleThreadExecutor();
	
	private static final ISchedulingRule NULL_RULE = new ISchedulingRule() {
		public boolean contains(ISchedulingRule rule) {
			return rule == this;
		}

		public boolean isConflicting(ISchedulingRule rule) {
			return rule == this;
		}
	};
	
	private WorkspaceRunner() {
		// No instantiation
	}
	
	// TODO: Invocations WorkspaceRunner should specify a progress monitor

	/**
	 * Atomically run an operation in the workspace thread.
	 */
	public static void run(final IWorkspaceRunnable action) {
			final IWorkspace workspace = ResourcesPlugin.getWorkspace();
			threadLauncher.execute(
				new Runnable() {
					public void run() {
						launchAfterUnlock(action, workspace);
					}
				});
	}

	/**
	 * Wait for the workspace to be unlocked,
	 * then create a new workspace job.
	 */
	private static void launchAfterUnlock(final IWorkspaceRunnable action, final IWorkspace workspace) {
		try {
			workspace.run(
				  new IWorkspaceRunnable() {
					public void run(IProgressMonitor monitor) throws CoreException {
						waitForUnlock(workspace);
						action.run(monitor);
					}
				  }
				, NULL_RULE // FIXME: non-'null' rule requires cancellation support
				, IWorkspace.AVOID_UPDATE // avoid workspace lock being reacquired
				, null
				);
		} catch (CoreException e) {
			Environment.logException("Could not run workspace job", e);
		}
	}

	/**
	 * Wait for the workspace to be unlocked.
	 * (The PRE_BUILD and POST_BUILD events associated with this 
	 * are not always fired; therefore this method simply waits.)
	 */
	private static void waitForUnlock(final IWorkspace workspace) {
		while (workspace.isTreeLocked()) {
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		}
	}
}
