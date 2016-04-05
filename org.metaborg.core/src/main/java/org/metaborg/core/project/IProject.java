package org.metaborg.core.project;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.config.IProjectConfig;

public interface IProject {
    /**
     * Gets the location of the root folder of the project.
     *
     * @return Location of the root folder.
     */
    FileObject location();

    /**
     * Gets the configuration of the project. The configuration is read only once when this class is instantiated. To
     * get a new configuration, get a new instance of this class.
     * 
     * @return Configuration of the project, or null when there is no configuration for this project.
     */
    @Nullable IProjectConfig config();
}
