package org.metaborg.spoofax.meta.core.config;

import javax.annotation.Nullable;

import org.metaborg.core.config.ConfigException;
import org.metaborg.meta.core.project.ILanguageSpec;
import org.metaborg.spoofax.meta.core.config.ISpoofaxLanguageSpecConfig;
import org.metaborg.util.file.IFileAccess;

/**
 * Writes a configuration for the specified {@link ILanguageSpec}.
 */
public interface ISpoofaxLanguageSpecConfigWriter {
    /**
     * Checks if a configuration file already exists for given language specification project.
     * 
     * @param languageSpec
     *            Language specification project.
     * @return True if configuration file exists, false otherwise.
     */
    boolean exists(ILanguageSpec languageSpec);

    /**
     * Writes the specified configuration for the specified language specification.
     *
     * @param languageSpec
     *            The language specification.
     * @param config
     *            The configuration to write.
     * @param access
     */
    void write(ILanguageSpec languageSpec, ISpoofaxLanguageSpecConfig config, @Nullable IFileAccess access)
        throws ConfigException;
}
