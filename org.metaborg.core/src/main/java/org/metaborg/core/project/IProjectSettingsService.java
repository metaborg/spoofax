package org.metaborg.core.project;

/**
 * Interface for getting project settings.
 */
public interface IProjectSettingsService {
    /**
     * Retrieves project settings given a project.
     * 
     * @param project
     *            Project to get settings for
     * @return Project settings.
     * @throws ProjectException
     *             When creating project settings unexpectedly fails.
     */
    public abstract IProjectSettings get(IProject project) throws ProjectException;
}
