package org.metaborg.core.project.config;

import java.io.IOException;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;

/**
 * Stores and retrieves language component configurations.
 */
public interface ILanguageComponentConfigService {
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
