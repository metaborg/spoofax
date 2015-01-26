package org.metaborg.spoofax.eclipse.processing;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;

public class BuildJob extends Job {
    public BuildJob() {
        super("Building Spoofax files");
    }

    @Override protected IStatus run(IProgressMonitor monitor) {
        // TODO Auto-generated method stub
        return null;
    }
}
