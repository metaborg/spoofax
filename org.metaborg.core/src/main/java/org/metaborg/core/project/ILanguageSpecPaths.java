package org.metaborg.core.project;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.resource.IResourceService;

import java.io.Serializable;

/**
 * Specifies the paths used in a language specification.
 */
public interface ILanguageSpecPaths {

    // NOTE: There is no getter for the configuration file
    // as the ILanguageSpecPaths interface requires a configuration to begin with.

    // NOTE: Name for getter for
    // - filename String     ends with "Filename";
    // - path     String     ends with "Path";
    // - file     FileObject ends with "File";
    // - folder   FileObject ends with "Folder".

    /**
     * Gets the language specification's root folder.
     *
     * @return A folder {@link FileObject}.
     */
    FileObject rootFolder();

    /**
     * Gets the output folder.
     *
     * @return A folder {@link FileObject}.
     */
    FileObject outputFolder();

}
