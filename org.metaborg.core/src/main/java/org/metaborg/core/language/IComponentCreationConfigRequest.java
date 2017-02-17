package org.metaborg.core.language;

import java.util.Collection;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.config.ILanguageComponentConfig;

/**
 * Request for creating a {@link ComponentCreationConfig}.
 */
public interface IComponentCreationConfigRequest extends ILanguageDiscoveryRequest {
    /**
     * Gets whether a language component can be constructed from this request.
     *
     * A request is valid when all required files exist, such as the parse table and JAR files, and all configuration
     * files are valid.
     * 
     * @return <code>true</code> when a request is valid, <code>false</code> otherwise.
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
     * @return Location of the request.
     */
    FileObject location();

    /**
     * @return Language component configuration; or <code>null</code> when not available.
     */
    @Nullable ILanguageComponentConfig config();

    /**
     * Gets the errors produced during the creation of this request.
     *
     * The resulting collection is empty when {@link #valid()} is <code>true</code>.
     *
     * @return Produced error messages.
     */
    Collection<String> errors();

    /**
     * Gets the exceptions thrown during the creation of this request.
     *
     * The resulting collection is empty when {@link #valid()} is <code>true</code>.
     *
     * @return Thrown exceptions.
     */
    Collection<Throwable> exceptions();
}
