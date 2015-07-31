package org.metaborg.spoofax.core.project.settings;

import org.apache.maven.project.MavenProject;
import org.metaborg.core.project.IProject;
import org.metaborg.core.project.ProjectException;
import org.metaborg.spoofax.core.project.IMavenProjectService;

import com.google.inject.Inject;

public class SpoofaxProjectSettingsService implements ISpoofaxProjectSettingsService {
    private final IMavenProjectService mavenProjectService;


    @Inject public SpoofaxProjectSettingsService(IMavenProjectService mavenProjectService) {
        this.mavenProjectService = mavenProjectService;
    }


    @Override public SpoofaxProjectSettings get(IProject project) throws ProjectException {
        final MavenProject mavenProject = mavenProjectService.get(project);
        if(mavenProject == null) {
            final String message =
                String.format("Could not retrieve Maven project for %s, cannot get settings", project);
            throw new ProjectException(message);
        }

        return MavenProjectSettingsReader.spoofaxSettings(project.location(), mavenProject);
    }
}
