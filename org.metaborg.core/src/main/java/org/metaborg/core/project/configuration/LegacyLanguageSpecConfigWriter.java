package org.metaborg.core.project.configuration;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.metaborg.core.project.ILanguageSpec;
import org.metaborg.core.project.settings.IProjectSettings;
import org.metaborg.util.file.FileAccess;

import javax.annotation.Nullable;
import java.io.IOException;


@SuppressWarnings("deprecation")
public class LegacyLanguageSpecConfigWriter implements ILanguageSpecConfigWriter {

    @Override
    public void write(ILanguageSpec languageSpec, ILanguageSpecConfig config, @Nullable FileAccess access) throws IOException {
        FileObject settingsFile = getConfigFile(languageSpec);
        if (!(config instanceof LegacyLanguageSpecConfig))
            throw new RuntimeException("This class can only deal with LegacySpoofaxLanguageSpecConfig configurations.");

        final IProjectSettings settings = ((LegacyLanguageSpecConfig) config).settings;
        throw new UnsupportedOperationException("Not implemented!");
    }

    @Override
    public FileObject getConfigFile(ILanguageSpec languageSpec) throws FileSystemException {
        throw new UnsupportedOperationException("Not implemented!");
    }
}
