package org.metaborg.meta.core.config;

import java.io.IOException;

import javax.annotation.Nullable;

import org.metaborg.meta.core.project.ILanguageSpec;
import org.metaborg.util.file.FileAccess;

/**
 * Writes a configuration for the specified {@link ILanguageSpec}.
 */
public interface ILanguageSpecConfigWriter {
    /**
     * Writes the specified configuration for the specified language specification.
     *
     * @param languageSpec
     *            The language specification.
     * @param config
     *            The configuration to write.
     * @param access
     */
    void write(ILanguageSpec languageSpec, ILanguageSpecConfig config, @Nullable FileAccess access) throws IOException;
}
