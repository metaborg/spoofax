package org.metaborg.core.project.configuration;

import java.io.IOException;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.language.ILanguageComponent;

/**
 * Stores and retrieves language component configurations.
 */
public interface ILanguageComponentConfigService {
    /**
     * Gets the configuration for the given language component.
     *
     * @param languageComponent
     *            The language component to get the configuration for.
     * @return The configuration; or <code>null</code> when no configuration could be retrieved.
     */
    @Nullable ILanguageComponentConfig get(ILanguageComponent languageComponent) throws IOException;

    /**
     * Gets the configuration for the language component at the given location.
     *
     * @param rootFolder
     *            The language component root folder.
     * @return The configuration; or <code>null</code> when no configuration could be retrieved.
     * @throws IOException
     */
    @Nullable ILanguageComponentConfig get(FileObject rootFolder) throws IOException;
}
