package org.metaborg.spoofax.eclipse.job;

import org.eclipse.core.runtime.jobs.ISchedulingRule;

/**
 * Scheduling rule that mimics a read lock of a read/write lock.
 */
public class ReadLockRule implements ISchedulingRule {
    private final LockRule writeLock;
    private final String name;


    public ReadLockRule(LockRule writeLock, String name) {
        this.writeLock = writeLock;
        this.name = name;
    }


    @Override public boolean isConflicting(ISchedulingRule rule) {
        return rule == this || rule == writeLock;
    }

    @Override public boolean contains(ISchedulingRule rule) {
        return rule == this;
    }

    @Override public String toString() {
        return name;
    }
}
