package org.metaborg.spoofax.core.project.settings;

import javax.annotation.Nullable;

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


    @Override public @Nullable SpoofaxProjectSettings get(IProject project) throws ProjectException {
        final MavenProject mavenProject = mavenProjectService.get(project);
        if(mavenProject == null) {
            final String message =
                String.format("Could not retrieve Maven project for %s, cannot get settings", project);
            throw new ProjectException(message);
        }

        final SpoofaxProjectSettings settings =
            MavenProjectSettingsReader.spoofaxSettings(project.location(), mavenProject);
        if(settings == null) {
            throw new ProjectException("Could not get settings, Maven project settings reader returned null");
        }
        return settings;
    }
}
