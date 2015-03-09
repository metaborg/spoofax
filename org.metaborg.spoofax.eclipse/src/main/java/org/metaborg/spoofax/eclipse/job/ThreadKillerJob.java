package org.metaborg.spoofax.eclipse.job;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;
import org.metaborg.spoofax.eclipse.util.StatusUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThreadKillerJob extends Job {
    private static final Logger logger = LoggerFactory.getLogger(ThreadKillerJob.class);

    private final Thread thread;


    public ThreadKillerJob(Thread thread) {
        super("Killing thread");
        setSystem(true);
        setPriority(INTERACTIVE);

        this.thread = thread;
    }


    @Override protected IStatus run(IProgressMonitor monitor) {
        if(monitor.isCanceled())
            return StatusUtils.cancel();
        logger.warn("Killing {}", thread);
        thread.stop();
        return StatusUtils.success();
    }
}
