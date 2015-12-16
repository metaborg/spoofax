package org.metaborg.core.project;

import org.apache.commons.vfs2.FileObject;

/**
 * Specifies the paths used in a language specification.
 */
public interface ILanguageSpecPaths {

    // NOTE: There is no getter for the configuration file
    // as the ILanguageSpecPaths interface requires a configuration to begin with.

    /**
     * Gets the language specification's root folder.
     *
     * @return The root folder.
     */
    FileObject rootFolder();

    /**
     * Gets the output folder.
     *
     * @return The output folder.
     */
    FileObject outputFolder();

}
