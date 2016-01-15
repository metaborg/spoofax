package org.metaborg.core.project.configuration;

import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.metaborg.core.project.ILanguageSpec;
import java.io.IOException;

import org.metaborg.core.project.ILanguageSpec;
import org.metaborg.util.file.FileAccess;

import javax.annotation.Nullable;

/**
 * Writes a configuration for the specified {@link ILanguageSpec}.
 */
public interface ILanguageSpecConfigWriter {

    /**
     * Writes the specified configuration for the specified language specification.
     *
     * @param languageSpec The language specification.
     * @param config The configuration to write.
     * @param access
     */
    void write(ILanguageSpec languageSpec, ILanguageSpecConfig config, @Nullable FileAccess access) throws IOException;

    /**
     * Gets the configuration file where the configuration is stored.
     *
     * @param languageSpec The language specification.
     * @return The configuration file; or <code>null</code> if the configuration
     * is not stored in a file.
     */
    FileObject getConfigFile(ILanguageSpec languageSpec) throws FileSystemException;

}
