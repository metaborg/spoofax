package org.strategoxt.imp.runtime.services;

import java.util.concurrent.CancellationException;
import java.util.concurrent.PriorityBlockingQueue;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.imp.parser.IParseController;
import org.strategoxt.imp.runtime.Environment;
import org.strategoxt.imp.runtime.MonitorStateWatchDog;

/**
 * A workbench-global queue of Stratego operations.
 * 
 * Public methods are thread-safe due to the usage of a PriorityBlockingQueue.
 * 
 * @author nathan
 * 
 */
public class StrategoAnalysisQueue {

	/**
	 * TODO: - remove background jobs if a foreground job is started for the
	 * same file - stop analyzing on a workspace close (/eclipse exit) -
	 * interrupt background jobs for foreground jobs ?
	 */

	private static final long serialVersionUID = 1L;

	private final PriorityBlockingQueue<UpdateJob> queue;

	/*
	 * Indicates whether a job is currently running.
	 * Only one job can be running at any given time.
	 */
	private volatile boolean running = false;

	private class UpdateJob extends Job {

		private static final int BACKGROUND = LONG;

		private final StrategoAnalysisJob job;

		private final long delay;
		
		private boolean cancelled;

		protected UpdateJob(StrategoAnalysisJob job, IPath path, int priority, boolean isSystem,
				long delay) {
			super("");
			this.job = job;
			this.delay = delay;

			// Should be set before scheduling the job
			this.setName(JOB_DESCRIPTION + path);

			setSystem(isSystem);
			setPriority(priority);
		}

		final static String JOB_DESCRIPTION = "Analyzing updates to ";

		@Override
		protected IStatus run(IProgressMonitor monitor) {

			running = true;
			if (!isSystem())
				MonitorStateWatchDog.protect(this, monitor, job.getObserver());

			IStatus status;
			try {
				status = job.analyze(monitor);
			} catch (CancellationException e) {
				status = Status.CANCEL_STATUS;
			} catch (RuntimeException e) {
				Environment.logException("Error running scheduled analysis", e);
				status = Status.CANCEL_STATUS;
			} catch (Error e) {
				Environment.logException("Error running scheduled analysis", e);
				status = Status.CANCEL_STATUS;
			}

			// Run next task
			running = false;
			wake();

			return status;
		}

		public void scheduleWithDelay() {
			super.schedule(this.delay);
		}
		
		@Override
		protected void canceling() {
			cancelled = true;
		}
		
		public boolean isCancelled() {
			return cancelled;
		}

	}

	public StrategoAnalysisQueue() {
		super();
		this.queue = new PriorityBlockingQueue<UpdateJob>();
	}

	/**
	 * Queue analysis of the file, interactively. Used for analyzing the contents of the current editor.
	 * @param observer the file's observer
	 * @param parseController the parse controller of the file
	 * @param delay delay before starting, in milliseconds
	 * @return The job. Can be used to call {@link UpdateJob#cancel()}.
	 */
	public UpdateJob queue(StrategoObserver observer, IParseController parseController, long delay) {

		// File has changed.
		// Schedule with high priority.

		IPath path = parseController.getPath();

		StrategoObserverUpdateJob job = new StrategoObserverUpdateJob(observer);
		job.setup(parseController);

		// To avoid progress view spamming, we only show jobs in the progress view if they have been
		// dynamically loaded
		boolean isSystemJob = !Environment.getDescriptor(parseController.getLanguage()).isDynamicallyLoaded();
		UpdateJob updateJob = new UpdateJob(job, path, UpdateJob.INTERACTIVE, isSystemJob, delay);
		add(updateJob);
		return updateJob;

	}

	/**
	 * Queue some Stratego job in the background.
	 * @param job the job to perform
	 * @param project the project to which the job belongs
	 * @return the job
	 */
	public UpdateJob queue(StrategoAnalysisJob job, IProject project) {

		IPath path = project.getFullPath();
		UpdateJob updateJob = new UpdateJob(job, path, UpdateJob.BACKGROUND, false, 0);
		add(updateJob);
		return updateJob;

	}

	private void add(UpdateJob job) {

		this.queue.add(job);
		this.wake();

	}

	private void wake() {
		if (running)
			return;

		UpdateJob job = queue.poll();
		if (job != null) {
			if (job.isCancelled()) {
				wake();
			} else {
				job.scheduleWithDelay(); // calls wake()
			}
		}
	}

	protected static IPath fullPathToWorkspaceLocal(IPath fullPath) {

		IPath workspace = ResourcesPlugin.getWorkspace().getRoot().getLocation();
		return fullPath.removeFirstSegments(fullPath.matchingFirstSegments(workspace));
	}

	protected static IResource pathToResource(IPath localPath) {

		return ResourcesPlugin.getWorkspace().getRoot().findMember(localPath);

	}

	/**
	 * Queue background analysis for a given file.
	 * @param path the file's path
	 * @param project the file's project
	 * @return the update job
	 */
	public UpdateJob queueAnalysis(IPath path, IProject project) {

		StrategoObserverBackgroundUpdateJob job = new StrategoObserverBackgroundUpdateJob(path, project);

		UpdateJob updateJob = new UpdateJob(job, path, UpdateJob.BACKGROUND, true, 0);
		add(updateJob);
		wake();

		return updateJob;
	}

}
