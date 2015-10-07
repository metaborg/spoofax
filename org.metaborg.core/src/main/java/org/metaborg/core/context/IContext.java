package org.metaborg.core.context;

import java.io.IOException;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.util.concurrent.IClosableLock;

import com.google.inject.Injector;

/**
 * Interface for a context in which analysis or transformations occurs. Implementors must override {@link #hashCode()}
 * and {@link #equals(Object)} using {@link #location()} and {@link #language()}, and also implement
 * {@link IContextInternal}.
 */
public interface IContext {
    /**
     * @return Location of this context.
     */
    public abstract FileObject location();

    /**
     * @return Language of this context.
     */
    public abstract ILanguageImpl language();

    /**
     * @return Injector to retrieve implementations.
     */
    public abstract Injector injector();


    /**
     * Request read access to this context.
     * 
     * @return Closable lock which must be held during reading. Close the lock when done.
     */
    public abstract IClosableLock read();

    /**
     * Request write access to this context.
     * 
     * @return Closable lock which must be held during writing. Close the lock when done.
     */
    public abstract IClosableLock write();

    /**
     * Persist context data from memory to permanent storing. Acquires a read lock. Can be called while holding the
     * write lock.
     * 
     * @throws IOException
     *             When persisting fails unexpectedly.
     */
    public abstract void persist() throws IOException;

    /**
     * Resets the state of this context. Acquires a write lock. Cannot be called while holding the read lock.
     * 
     * @throws IOException
     *             When resetting fails unexpectedly
     */
    public abstract void reset() throws IOException;


    /* Hint for hashCode implementation. */
    public abstract int hashCode();

    /* Hint for equals implementation. */
    public abstract boolean equals(Object other);
}
