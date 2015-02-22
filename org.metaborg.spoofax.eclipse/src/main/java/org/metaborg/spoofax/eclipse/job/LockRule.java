package org.metaborg.spoofax.eclipse.job;

import org.eclipse.core.runtime.jobs.ISchedulingRule;

/**
 * Scheduling rule that mimics a lock.
 */
public class LockRule implements ISchedulingRule {
    private final String name;


    public LockRule(String name) {
        this.name = name;
    }


    @Override public boolean isConflicting(ISchedulingRule rule) {
        return rule == this;
    }

    @Override public boolean contains(ISchedulingRule rule) {
        return rule == this;
    }

    @Override public String toString() {
        return name;
    }
}
