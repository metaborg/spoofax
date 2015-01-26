package org.metaborg.spoofax.eclipse.processing;

import org.eclipse.core.runtime.jobs.ISchedulingRule;

public class MutexRule implements ISchedulingRule {
    @Override public boolean contains(ISchedulingRule rule) {
        return this == rule;
    }

    @Override public boolean isConflicting(ISchedulingRule rule) {
        return this == rule;
    }
}
