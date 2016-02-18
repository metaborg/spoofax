package org.metaborg.spoofax.core.project;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.apache.maven.project.MavenProject;
import org.metaborg.core.project.IProject;

/**
 * Interface for retrieving Maven projects.
 */
@Deprecated
public interface ILegacyMavenProjectService {
    /**
     * Gets a Maven project for given Metaborg project.
     * 
     * @param project
     *            Metaborg project to get Maven project for.
     * @return Maven project, or null if no Maven project could be retrieved. When there is no (valid) POM file in the
     *         project, null is returned.
     */
    @Nullable MavenProject get(FileObject location);

    @Nullable MavenProject get(IProject project);
}
