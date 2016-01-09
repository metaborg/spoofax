package org.metaborg.spoofax.core.project.settings;

import java.io.IOException;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.apache.maven.project.MavenProject;
import org.metaborg.core.project.IProject;
import org.metaborg.core.project.settings.IProjectSettings;
import org.metaborg.core.project.settings.IProjectSettingsService;
import org.metaborg.core.project.settings.YAMLProjectSettingsSerializer;
import org.metaborg.spoofax.core.project.IMavenProjectService;
import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.LoggerUtils;

import com.google.inject.Inject;

@Deprecated
public class ProjectSettingsService implements IProjectSettingsService {
    private static final ILogger logger = LoggerUtils.logger(ProjectSettingsService.class);

    private final IMavenProjectService mavenProjectService;


    @Inject public ProjectSettingsService(IMavenProjectService mavenProjectService) {
        this.mavenProjectService = mavenProjectService;
    }


    @Override public @Nullable IProjectSettings get(IProject project) {
        final MavenProject mavenProject = mavenProjectService.get(project);
        if(mavenProject == null) {
            logger.trace("Could not retrieve Maven project for {}, cannot get settings", project);
            return null;
        }

        final SpoofaxProjectSettings settings = MavenProjectSettingsReader.spoofaxSettings(project.location(), mavenProject);
        if(settings != null) {
            return settings.settings();
        }
        return null;
    }

    @Override public @Nullable IProjectSettings get(FileObject location) {
        try {
            final FileObject settingsFile = location.resolveFile("src-gen/metaborg.generated.yaml");
            if(!settingsFile.exists()) {
                return null;
            }
            final IProjectSettings settings = YAMLProjectSettingsSerializer.read(settingsFile);
            return settings;
        } catch(IOException e) {
            final String message =
                String.format("Reading settings file %s/src-gen/metaborg.generated.yaml failed unexpectedly", location);
            logger.warn(message, e);
            return null;
        }
    }
}
