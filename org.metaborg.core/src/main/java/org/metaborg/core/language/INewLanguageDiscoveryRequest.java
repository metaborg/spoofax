package org.metaborg.core.language;

import java.util.Collection;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.project.configuration.ILanguageComponentConfig;

/**
 * Request for language discovery.
 */
public interface INewLanguageDiscoveryRequest {

    /**
     * Gets whether the language component is available.
     *
     * A language component is available when all required files exist,
     * such as the parse table and JAR files,
     * and all configuration files are valid.
     * 
     * @return <code>true</code> when a request is available;
     * otherwise, <code>false</code>.
     */
    boolean available();

    /**
     * Gets the location of the request.
     *
     * @return The location of the request.
     */
    FileObject location();

    /**
     * Gets the configuration of the language.
     *
     * @return The configuration; or <code>null</code> when the language is not available.
     */
    @Nullable
    ILanguageComponentConfig config();

    /**
     * Gets the errors produced during the creation of this request.
     *
     * The resulting collection is empty when {@link #available()} is <code>true</code>.
     *
     * @return The produced error messages.
     */
    Collection<String> errors();

    /**
     * Gets the exceptions thrown during the creation of this request.
     *
     * The resulting collection is empty when {@link #available()} is <code>true</code>.
     *
     * @return The thrown exceptions.
     */
    Collection<Throwable> exceptions();
    
    /**
     * Gets a summary of the errors and exceptions in this request.
     *
     * The resulting string is empty when {@link #available()} is <code>true</code>.
     *
     * @return The summary of errors and exceptions in this request;
     * or an empty string when the request is available.
     *
     * @deprecated Call {@link #toString()} instead.
     */
    @Deprecated
    String errorSummary();
}
