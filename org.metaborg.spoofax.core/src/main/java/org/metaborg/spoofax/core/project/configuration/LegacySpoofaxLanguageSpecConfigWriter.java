package org.metaborg.spoofax.core.project.configuration;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.metaborg.core.project.ILanguageSpec;
import org.metaborg.core.project.settings.LegacyProjectSettings;
import org.metaborg.core.project.settings.YAMLLegacyProjectSettingsSerializer;
import org.metaborg.spoofax.core.project.settings.LegacySpoofaxProjectSettings;
import org.metaborg.util.file.FileAccess;

import javax.annotation.Nullable;
import java.io.IOException;

@SuppressWarnings("deprecation")
public class LegacySpoofaxLanguageSpecConfigWriter implements ISpoofaxLanguageSpecConfigWriter {

    @Override
    public void write(ILanguageSpec languageSpec, ISpoofaxLanguageSpecConfig config, @Nullable FileAccess access) throws IOException {
        final LegacySpoofaxProjectSettings settings = getSettings(languageSpec.location(), config);

        FileObject settingsFile = getConfigFile(languageSpec);
        settingsFile.createFile();
        YAMLLegacyProjectSettingsSerializer.write(settingsFile, settings.settings());
        if (access != null)
            access.addWrite(settingsFile);
    }

    @Override
    public FileObject getConfigFile(ILanguageSpec languageSpec) throws FileSystemException {
        return languageSpec.location().resolveFile("src-gen").resolveFile("metaborg.generated.yaml");
    }

    private LegacySpoofaxProjectSettings getSettings(FileObject location, ISpoofaxLanguageSpecConfig config) {
        if (config instanceof LegacySpoofaxLanguageSpecConfig) {
            return ((LegacySpoofaxLanguageSpecConfig) config).settings;
        } else {
            return new LegacySpoofaxProjectSettings(new LegacyProjectSettings(
                    config.identifier(),
                    config.name(),
                    config.compileDependencies(),
                    config.runtimeDependencies(),
                    config.languageContributions()
            ), location);
        }
    }
}
