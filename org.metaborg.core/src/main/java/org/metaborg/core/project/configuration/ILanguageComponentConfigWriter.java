package org.metaborg.core.project.configuration;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.metaborg.core.language.ILanguageComponent;
import org.metaborg.core.project.ILanguageSpec;
import java.io.IOException;

import org.metaborg.core.language.ILanguageComponent;

/**
 * Writes a configuration for the specified {@link ILanguageComponent}.
 */
public interface ILanguageComponentConfigWriter {

    /**
     * Writes the specified configuration for the specified language component.
     *
     * @param languageComponent The language component.
     * @param config The configuration to write.
     */
    void write(ILanguageComponent languageComponent, ILanguageComponentConfig config) throws IOException;

    /**
     * Gets the configuration file where the configuration is stored.
     *
     * @param languageComponent The language component.
     * @return The configuration file; or <code>null</code> if the configuration
     * is not stored in a file.
     */
    FileObject getConfigFile(ILanguageComponent languageComponent) throws FileSystemException;

}
