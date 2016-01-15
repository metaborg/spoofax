package org.metaborg.core.language;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;

/**
 * Request for language discovery.
 *
 * @deprecated Use {@link INewLanguageDiscoveryRequest} instead.
 */
@Deprecated
public interface ILanguageDiscoveryRequest {
    /**
     * A request is available when all required files exist, such as the parse table and JAR files, and all
     * configuration files are valid.
     * 
     * @return True when available, false if not.
     */
    public abstract boolean available();

    /**
     * @return Location of the request.
     */
    public abstract FileObject location();

    /**
     * @return Error messages produced during the creation of this request.
     */
    public abstract Iterable<String> errors();

    /**
     * @return Exceptions thrown during the creation of this request.
     */
    public abstract Iterable<Throwable> exceptions();
    
    /**
     * @return Summary of the errors and exceptions in this request, or null if the request is available.
     */
    public abstract @Nullable String errorSummary();
}
