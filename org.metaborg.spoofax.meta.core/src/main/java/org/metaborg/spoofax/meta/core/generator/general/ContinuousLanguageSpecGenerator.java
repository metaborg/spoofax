package org.metaborg.spoofax.meta.core.generator.general;

import java.io.IOException;

import javax.annotation.Nullable;

import org.metaborg.spoofax.meta.core.config.SdfVersion;
import org.metaborg.spoofax.meta.core.generator.BaseGenerator;
import org.metaborg.spoofax.meta.core.generator.GeneratorSettings;
import org.metaborg.util.file.IFileAccess;

/**
 * Generates project files which need to be generated after each build. Files are not specific to an IDE.
 */
public class ContinuousLanguageSpecGenerator extends BaseGenerator {
    private final boolean sdfEnabled;
    private final @Nullable SdfVersion version;


    public ContinuousLanguageSpecGenerator(GeneratorSettings scope, IFileAccess access, boolean sdfEnabled,
        @Nullable SdfVersion version) {
        super(scope, access);
        this.sdfEnabled = sdfEnabled;
        this.version = version;
    }

    public ContinuousLanguageSpecGenerator(GeneratorSettings scope, boolean sdfEnabled, @Nullable SdfVersion version) {
        super(scope);
        this.sdfEnabled = sdfEnabled;
        this.version = version;
    }


    public void generateAll() throws IOException {
        if(sdfEnabled && version == SdfVersion.sdf3) {
            generateCompletionStrategies();
        }
    }


    public void generateCompletionStrategies() throws IOException {
        writer.write("langspec/src-gen/completion/completion.str", "src-gen/completion/completion.str", true);
    }
}
