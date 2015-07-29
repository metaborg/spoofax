package org.metaborg.spoofax.core.project;

import org.metaborg.core.project.IProject;
import org.metaborg.core.project.IProjectSettingsService;
import org.metaborg.core.project.ProjectException;

public interface ISpoofaxProjectSettingsService extends IProjectSettingsService {
    /**
     * Retrieves Spoofax project settings given a project.
     * 
     * @param project
     *            Project to get settings for
     * @return Spoofax project settings.
     * @throws ProjectException
     *             When creating project settings unexpectedly fails.
     */
    public abstract SpoofaxProjectSettings get(IProject project) throws ProjectException;
}
