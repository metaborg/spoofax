package org.metaborg.spoofax.eclipse.processing;

import org.eclipse.core.runtime.jobs.ISchedulingRule;

public class MutexRule implements ISchedulingRule {
    private final String name;


    public MutexRule(String name) {
        this.name = name;
    }


    @Override public boolean contains(ISchedulingRule rule) {
        return this == rule;
    }

    @Override public boolean isConflicting(ISchedulingRule rule) {
        return this == rule;
    }

    @Override public String toString() {
        return name;
    }
}
