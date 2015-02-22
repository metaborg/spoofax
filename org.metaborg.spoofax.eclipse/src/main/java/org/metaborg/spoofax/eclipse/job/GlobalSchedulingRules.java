package org.metaborg.spoofax.eclipse.job;

import java.util.concurrent.ConcurrentMap;

import org.metaborg.spoofax.core.context.IContext;

import com.google.common.collect.Maps;

/**
 * Collection of global scheduling rules.
 */
public class GlobalSchedulingRules {
    private final LockRule startupLock = new LockRule("Startup write");
    private final LockRule languageServiceLock = new LockRule("Language service write");
    private final ConcurrentMap<IContext, LockRule> contextLocks = Maps.newConcurrentMap();


    /**
     * Returns the startup read/write lock rule, acquired during start up to load all languages in the workspace
     * dynamically. Use {@link #startupReadLock()} to get a read-only lock rule.
     * 
     * @return Startup read/write lock scheduling rule.
     */
    public LockRule startupWriteLock() {
        return startupLock;
    }

    /**
     * Returns a new read-only lock rule, which blocks during startup, and never blocks after that. Use to schedule jobs
     * after startup, when all languages in the workspace have been loaded dynamically.
     * 
     * @return New startup read-only lock scheduling rule.
     */
    public ReadLockRule startupReadLock() {
        return new ReadLockRule(startupLock, "Startup read");
    }

    /**
     * Returns the read/write lock rule for exclusive access to the language service, which is not thread-safe. Use to
     * schedule jobs that require the language service.
     * 
     * @return Language service read/write lock scheduling rule.
     */
    public LockRule languageServiceLock() {
        return languageServiceLock;
    }

    /**
     * Returns the read/write lock rule for given context.
     * 
     * @param context
     *            Context to get the lock for.
     * @return Context read/write lock scheduling rule.
     */
    public LockRule contextLock(IContext context) {
        final LockRule newRule = new LockRule(context.toString());
        final LockRule prevRule = contextLocks.putIfAbsent(context, newRule);
        return prevRule != null ? prevRule : newRule;
    }
}
