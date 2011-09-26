package org.strategoxt.imp.runtime;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.spoofax.IAsyncCancellable;

/**
 * @author Lennart Kats <lennart add lclnet.nl>
 * @author Tobi Vollebregt
 */
public class MonitorStateWatchDog {

	private static final int NO_TIMEOUT = -1;

	private static final int RUNNING_CHECK_INTERVAL = 500;

	private static final int CANCELLED_CHECK_INTERVAL = 20;

	private final Job runningJob;
	
	private final IProgressMonitor progressMonitor;

	private final IAsyncCancellable canceller;

	private final long deadline;

	private volatile boolean isProtecting;

	private volatile boolean isAsyncCancelling;

	public MonitorStateWatchDog(Job runningJob, IProgressMonitor progressMonitor, IAsyncCancellable canceller) {
		this(runningJob, progressMonitor, canceller, NO_TIMEOUT);
	}

	public MonitorStateWatchDog(Job runningJob, IProgressMonitor progressMonitor, IAsyncCancellable canceller, int timeoutMillis) {
		this.runningJob = runningJob;
		this.progressMonitor = progressMonitor;
		this.canceller = canceller;
		this.deadline = timeoutMillis == NO_TIMEOUT ? 0L : System.currentTimeMillis() + timeoutMillis;
		beginProtect();
	}

	/**
	 * Begins protecting this job, canceling it if the monitor is cancelled.
	 * Must be balanced with a call to {@link #endProtect()}.
	 */
	private void beginProtect() {
		if (isProtecting)
			throw new IllegalStateException("Already protecting");
		if (progressMonitor.isCanceled())
			return; // already cancelled; no need to kill it
		isProtecting = true;
		WatchDogJob job = new WatchDogJob();
		job.setSystem(true);
		// Eclipse adds extra latency to non-interactive jobs.
		job.setPriority(Job.INTERACTIVE);
		job.schedule(job.sleepHint());
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
			if (isAsyncCancelling) {
				stillCancelling();
			} else if (isProtecting && runningJob.getState() != Job.NONE) {
				stillRunning();
			}
			return Status.OK_STATUS;
		}

		private void stillRunning() {
			if (shouldBeCanceled()) {
				// canceled: start polling until job stopped running
				canceller.asyncCancel();
				progressMonitor.setCanceled(true);
				isAsyncCancelling = true;
				schedule(CANCELLED_CHECK_INTERVAL);
			} else {
				// not canceled (yet): re-schedule to check again
				schedule(sleepHint());
			}
		}

		private void stillCancelling() {
			if (isProtecting && runningJob.getState() != Job.NONE) {
				// re-schedule to check again
				schedule(CANCELLED_CHECK_INTERVAL);
			} else {
				// finished: job stopped running and endProtect() was called
				canceller.asyncCancelReset();
			}
		}

		private boolean shouldBeCanceled() {
			return progressMonitor.isCanceled()
					|| (deadline > 0L && System.currentTimeMillis() > deadline);
		}

		private long sleepHint() {
			if (deadline > 0) {
				long pending = deadline - System.currentTimeMillis();
				return Math.max(0L, Math.min(RUNNING_CHECK_INTERVAL, pending));
			}
			return RUNNING_CHECK_INTERVAL;
		}
	}

}
