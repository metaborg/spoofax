package org.metaborg.spoofax.core.project;

import org.apache.maven.project.MavenProject;
import org.metaborg.core.MetaborgException;
import org.metaborg.core.project.IProject;

/**
 * Specialization of the {@link ILegacyMavenProjectService} that supports creating and removing Maven projects. Maven project
 * instances are returned based on their project.
 */
@Deprecated
public interface ISimpleLegacyMavenProjectService extends ILegacyMavenProjectService {
    /**
     * Adds a Maven project for given Metaborg project.
     * 
     * @return Given Maven project.
     * 
     * @throws MetaborgException
     *             When a Maven project already exists for given Metaborg project.
     */
    MavenProject add(IProject project, MavenProject mavenProject) throws MetaborgException;

    /**
     * Removes Maven project for given Metaborg project.
     * 
     * @param project
     *            Metaborg project to remove Maven project for.
     * 
     * @throws MetaborgException
     *             When given Metaborg project does not exist in this project service.
     */
    void remove(IProject project) throws MetaborgException;
}
