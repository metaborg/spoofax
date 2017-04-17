package org.metaborg.core.language;

import java.util.Collection;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.config.ILanguageComponentConfig;

/**
 * Request for language discovery.
 * 
 * @deprecated Use {@link IComponentCreationConfigRequest} with {@link ILanguageComponentFactory}.
 */
@Deprecated
public interface ILanguageDiscoveryRequest {
    /**
     * Gets whether the language component is available.
     *
     * A language component is available when all required files exist, such as the parse table and JAR files, and all
     * configuration files are valid.
     * 
     * @return <code>true</code> when a request is available; otherwise, <code>false</code>.
     */
    boolean valid();
    
    /**
     * @see #valid()
     * @deprecated Use {@link #valid()}
     */
    @Deprecated default boolean available() {
        return valid();
    }

    /**
     * Gets the location of the request.
     *
     * @return The location of the request.
     */
    FileObject location();

    /**
     * Gets the configuration of the language.
     *
     * @return The configuration; or <code>null</code> when not available.
     */
    @Nullable ILanguageComponentConfig config();

    /**
     * Gets the errors produced during the creation of this request.
     *
     * The resulting collection is empty when {@link #valid()} is <code>true</code>.
     *
     * @return The produced error messages.
     */
    Collection<String> errors();

    /**
     * Gets the exceptions thrown during the creation of this request.
     *
     * The resulting collection is empty when {@link #valid()} is <code>true</code>.
     *
     * @return The thrown exceptions.
     */
    Collection<Throwable> exceptions();
}
