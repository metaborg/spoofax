package org.metaborg.spoofax.core.project.configuration;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.metaborg.core.project.ILanguageSpec;

import java.io.IOException;

/**
 * Writes a configuration for the specified {@link ILanguageSpec}.
 */
public interface ISpoofaxLanguageSpecConfigWriter {

    /**
     * Writes the specified configuration for the specified language specification.
     *
     * @param languageSpec The language specification.
     * @param config The configuration to write.
     */
    void write(ILanguageSpec languageSpec, ISpoofaxLanguageSpecConfig config) throws IOException;

    /**
     * Gets the configuration file where the configuration is stored.
     *
     * @param languageSpec The language specification.
     * @return The configuration file; or <code>null</code> if the configuration
     * is not stored in a file.
     */
    FileObject getConfigFile(ILanguageSpec languageSpec) throws FileSystemException;

}
