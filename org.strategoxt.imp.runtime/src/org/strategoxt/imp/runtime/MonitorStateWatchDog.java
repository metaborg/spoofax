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

	private MonitorStateWatchDog(Job runningJob, IProgressMonitor progressMonitor, IAsyncCancellable canceller) {
		this.runningJob = runningJob;
		this.progressMonitor = progressMonitor;
		this.canceller = canceller;
	}
	
	public static void protect(Job runningJob, IProgressMonitor progressMonitor, IAsyncCancellable canceller) {
		MonitorStateWatchDog watchDog = new MonitorStateWatchDog(runningJob, progressMonitor, canceller);
		Job job = watchDog.new WatchDogJob();
		job.setSystem(true);
		job.schedule(RUNNING_CHECK_INTERVAL);
	}
	
	private class WatchDogJob extends Job {
		
		public WatchDogJob() {
			super("MonitorStateWatchDog for " + runningJob.getName());
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			while (runningJob.getState() != Job.NONE) {
				if (progressMonitor.isCanceled()) {
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
		while (runningJob.getState() != Job.NONE) {
			try {
				Thread.sleep(CANCELLED_CHECK_INTERVAL);
			} catch (InterruptedException e) {
				break;
			}
		}
		canceller.asyncCancelReset();
	}

}
