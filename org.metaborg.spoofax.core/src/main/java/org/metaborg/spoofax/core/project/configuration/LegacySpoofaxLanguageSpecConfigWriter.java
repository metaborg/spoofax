package org.metaborg.spoofax.core.project.configuration;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.metaborg.core.project.ILanguageSpec;
import org.metaborg.core.project.settings.ProjectSettings;
import org.metaborg.core.project.settings.YAMLProjectSettingsSerializer;
import org.metaborg.spoofax.core.project.settings.SpoofaxProjectSettings;
import org.metaborg.util.file.FileAccess;

import javax.annotation.Nullable;
import java.io.IOException;

public class LegacySpoofaxLanguageSpecConfigWriter implements ISpoofaxLanguageSpecConfigWriter {

    @Override
    public void write(ILanguageSpec languageSpec, ISpoofaxLanguageSpecConfig config, @Nullable FileAccess access) throws IOException {
        final SpoofaxProjectSettings settings = getSettings(languageSpec.location(), config);

        FileObject settingsFile = getConfigFile(languageSpec);
        settingsFile.createFile();
        YAMLProjectSettingsSerializer.write(settingsFile, settings.settings());
        if (access != null)
            access.addWrite(settingsFile);
    }

    @Override
    public FileObject getConfigFile(ILanguageSpec languageSpec) throws FileSystemException {
        return languageSpec.location().resolveFile("src-gen").resolveFile("metaborg.generated.yaml");
    }

    private SpoofaxProjectSettings getSettings(FileObject location, ISpoofaxLanguageSpecConfig config) {
        if (config instanceof LegacySpoofaxLanguageSpecConfig) {
            return ((LegacySpoofaxLanguageSpecConfig) config).settings;
        } else {
            return new SpoofaxProjectSettings(new ProjectSettings(
                    config.identifier(),
                    config.name(),
                    config.compileDependencies(),
                    config.runtimeDependencies(),
                    config.languageContributions()
            ), location);
        }
    }
}
