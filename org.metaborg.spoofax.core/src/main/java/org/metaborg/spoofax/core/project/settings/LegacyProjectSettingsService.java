package org.metaborg.spoofax.core.project.settings;

import java.io.IOException;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.apache.maven.project.MavenProject;
import org.metaborg.core.project.IProject;
import org.metaborg.core.project.settings.ILegacyProjectSettings;
import org.metaborg.core.project.settings.ILegacyProjectSettingsService;
import org.metaborg.core.project.settings.YAMLLegacyProjectSettingsSerializer;
import org.metaborg.spoofax.core.project.ILegacyMavenProjectService;
import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.LoggerUtils;

import com.google.inject.Inject;

@SuppressWarnings("deprecation")
@Deprecated
public class LegacyProjectSettingsService implements ILegacyProjectSettingsService {
    private static final ILogger logger = LoggerUtils.logger(LegacyProjectSettingsService.class);

    private final ILegacyMavenProjectService mavenProjectService;


    @Inject public LegacyProjectSettingsService(ILegacyMavenProjectService mavenProjectService) {
        this.mavenProjectService = mavenProjectService;
    }


    @Override public @Nullable
    ILegacyProjectSettings get(IProject project) {
        final MavenProject mavenProject = mavenProjectService.get(project);
        if(mavenProject == null) {
            logger.trace("Could not retrieve Maven project for {}, cannot get settings", project);
            return null;
        }

        final LegacySpoofaxProjectSettings settings = LegacyMavenProjectSettingsReader.spoofaxSettings(project.location(), mavenProject);
        if(settings != null) {
            return settings.settings();
        }
        return null;
    }

    @Override public @Nullable
    ILegacyProjectSettings get(FileObject location) {
        try {
            final FileObject settingsFile = location.resolveFile("src-gen/metaborg.generated.yaml");
            if(!settingsFile.exists()) {
                return null;
            }
            final ILegacyProjectSettings settings = YAMLLegacyProjectSettingsSerializer.read(settingsFile);
            return settings;
        } catch(IOException e) {
            final String message =
                String.format("Reading settings file %s/src-gen/metaborg.generated.yaml failed unexpectedly", location);
            logger.warn(message, e);
            return null;
        }
    }
}
