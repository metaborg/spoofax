package org.metaborg.core.project;

import org.apache.commons.vfs2.FileObject;

/**
 * A project.
 */
public interface IProject {

    /**
     * Gets the location of the root folder of the project.
     *
     * @return Location of the root folder.
     */
    FileObject location();

}
