package org.metaborg.spoofax.core.project;

import javax.annotation.Nullable;

import org.apache.maven.project.MavenProject;
import org.metaborg.core.project.IProject;

/**
 * Interface for retrieving Maven projects.
 */
@Deprecated
public interface IMavenProjectService {
    /**
     * Gets a Maven project for given Metaborg project.
     * 
     * @param project
     *            Metaborg project to get Maven project for.
     * @return Maven project, or null if no Maven project could be retrieved. When there is no (valid) POM file in the
     *         project, null is returned.
     */
    public abstract @Nullable MavenProject get(IProject project);
}
