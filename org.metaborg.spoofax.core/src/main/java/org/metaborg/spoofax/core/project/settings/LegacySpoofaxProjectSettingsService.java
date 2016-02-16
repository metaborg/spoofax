package org.metaborg.spoofax.core.project.settings;

import javax.annotation.Nullable;

import org.apache.maven.project.MavenProject;
import org.metaborg.core.project.IProject;
import org.metaborg.core.project.ProjectException;
import org.metaborg.spoofax.core.project.ILegacyMavenProjectService;

import com.google.inject.Inject;

@SuppressWarnings("deprecation")
@Deprecated
public class LegacySpoofaxProjectSettingsService implements ILegacySpoofaxProjectSettingsService {
    private final ILegacyMavenProjectService mavenProjectService;


    @Inject public LegacySpoofaxProjectSettingsService(ILegacyMavenProjectService mavenProjectService) {
        this.mavenProjectService = mavenProjectService;
    }


    @Override public @Nullable LegacySpoofaxProjectSettings get(IProject project) throws ProjectException {
        final MavenProject mavenProject = mavenProjectService.get(project);
        if(mavenProject == null) {
            final String message =
                String.format("Could not retrieve Maven project for %s, cannot get settings", project);
            throw new ProjectException(message);
        }

        final LegacySpoofaxProjectSettings settings = LegacyMavenProjectSettingsReader.spoofaxSettings(mavenProject);
        if(settings == null) {
            throw new ProjectException("Could not get settings, Maven project settings reader returned null");
        }
        return settings;
    }
}
