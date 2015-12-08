package org.metaborg.core.project;

import org.apache.commons.vfs2.FileObject;

/**
 * Specifies the paths used in a language specification.
 */
public interface ILanguageSpecPaths {

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

    /**
     * Gets the language specification's configuration file.
     *
     * @return The configuration file.
     */
    FileObject configFile();

}
