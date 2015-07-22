package org.metaborg.core.context;

import java.io.Serializable;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.language.ILanguageImpl;

import com.google.inject.Injector;

/**
 * Interface for a context in which analysis or transformations occurs. Implementors must override {@link #hashCode()}
 * and {@link #equals(Object)} using {@link #location()} and {@link #language()}, and also implement
 * {@link IContextInternal}.
 */
public interface IContext extends Serializable {
    /**
     * @return Location of this context.
     */
    public abstract FileObject location();

    /**
     * @return Language of this context.
     */
    public abstract ILanguageImpl language();

    /**
     * @return Identifier of this context.
     */
    public abstract ContextIdentifier id();


    /**
     * @return Injector to retrieve implementations.
     */
    public abstract Injector injector();


    /**
     * Cleans given context, resetting its state.
     */
    public abstract void clean();


    /* Hint for hashCode implementation. */
    public abstract int hashCode();

    /* Hint for equals implementation. */
    public abstract boolean equals(Object other);
}
