package org.metaborg.spoofax.meta.core.config;

import java.io.IOException;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.config.ConfigRequest;
import org.metaborg.meta.core.config.ILanguageSpecConfigService;

/**
 * Stores and retrieves Spoofax language specification configurations.
 */
public interface ISpoofaxLanguageSpecConfigService extends ILanguageSpecConfigService {
    /**
     * Checks if a configuration exists for the Spoofax language specification at the given location.
     *
     * @param rootFolder
     *            The language specification root folder.
     * @return True if a configuration exists, false otherwise.
     * @throws IOException
     *             When location the configuration fails.
     */
    boolean available(FileObject rootFolder);

    /**
     * Gets the configuration for the Spoofax language specification at the given location.
     *
     * @param rootFolder
     *            The language specification root folder.
     * @return Configuration request, either with a valid configuration, or a collection of error messages.
     */
    ConfigRequest<ISpoofaxLanguageSpecConfig> get(FileObject rootFolder);
}
