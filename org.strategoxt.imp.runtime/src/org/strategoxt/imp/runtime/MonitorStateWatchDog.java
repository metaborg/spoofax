package org.strategoxt.imp.runtime;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.spoofax.IAsyncCancellable;

/**
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class MonitorStateWatchDog {

	private static final int RUNNING_CHECK_INTERVAL = 500;

	private static final int CANCELLED_CHECK_INTERVAL = 20;

	private final Job runningJob;
	
	private final IProgressMonitor progressMonitor;

	private final IAsyncCancellable canceller;
	
	private volatile boolean isProtecting;

	public MonitorStateWatchDog(Job runningJob, IProgressMonitor progressMonitor, IAsyncCancellable canceller) {
		this.runningJob = runningJob;
		this.progressMonitor = progressMonitor;
		this.canceller = canceller;
	}
	
	/**
	 * Begins protecting this job, cancelling it if the monitor is cancelled.
	 * Must be balanced with a call to {@link #endProtect()}.
	 */
	public void beginProtect() {
		if (isProtecting)
			throw new IllegalStateException("Already protecting");
		if (progressMonitor.isCanceled())
			return; // already cancelled; no need to kill it
		isProtecting = true;
		Job job = new WatchDogJob();
		job.setSystem(true);
		job.schedule(RUNNING_CHECK_INTERVAL);
	}
	
	/**
	 * Ends the protection of a job. Does not throw any exception.
	 */
	public void endProtect() {
		// Won't check this since we're likely being executed in a finally clause
		// if (!isProtecting) throw new IllegalStateException("Not protecting");
		isProtecting = false;
	}
	
	private class WatchDogJob extends Job {
		
		public WatchDogJob() {
			super("MonitorStateWatchDog for " + runningJob.getName());
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			while (isProtecting && runningJob.getState() != Job.NONE) {
				if (progressMonitor.isCanceled() && isProtecting) {
					asyncCancel();
					break;
				}
				try {
					Thread.sleep(RUNNING_CHECK_INTERVAL);
				} catch (InterruptedException e) {
					break;
				}
			}
			return Status.OK_STATUS;
		}
	}

	private void asyncCancel() {
		canceller.asyncCancel();
		while (runningJob.getState() != Job.NONE && isProtecting) {
			try {
				Thread.sleep(CANCELLED_CHECK_INTERVAL);
			} catch (InterruptedException e) {
				break;
			}
		}
		canceller.asyncCancelReset();
	}

}
