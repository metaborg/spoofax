package org.metaborg.spoofax.meta.core.generator.general;

import java.io.IOException;

import javax.annotation.Nullable;

import org.metaborg.spoofax.meta.core.config.SdfVersion;
import org.metaborg.spoofax.meta.core.config.StrategoVersion;
import org.metaborg.spoofax.meta.core.generator.BaseGenerator;
import org.metaborg.spoofax.meta.core.generator.GeneratorSettings;
import org.metaborg.util.file.IFileAccess;

/**
 * Generates project files which need to be generated after each build. Files are not specific to an IDE.
 */
public class ContinuousLanguageSpecGenerator extends BaseGenerator {
    private final boolean sdfEnabled;
    private final @Nullable SdfVersion sdfVersion;
    private final @Nullable StrategoVersion strategoVersion;


    public ContinuousLanguageSpecGenerator(GeneratorSettings scope, IFileAccess access, boolean sdfEnabled,
        @Nullable SdfVersion sdfVersion, @Nullable StrategoVersion strategoVersion) {
        super(scope, access);
        this.sdfEnabled = sdfEnabled;
        this.sdfVersion = sdfVersion;
        this.strategoVersion = strategoVersion;
    }

    public ContinuousLanguageSpecGenerator(GeneratorSettings scope, boolean sdfEnabled, @Nullable SdfVersion sdfVersion,
        @Nullable StrategoVersion strategoVersion) {
        super(scope);
        this.sdfEnabled = sdfEnabled;
        this.sdfVersion = sdfVersion;
        this.strategoVersion = strategoVersion;
    }


    public void generateAll() throws IOException {
        if(sdfEnabled && sdfVersion == SdfVersion.sdf3) {
            if(strategoVersion == StrategoVersion.v2) {
                generateStratego2CompletionStrategies();
            } else {
                generateCompletionStrategies();
            }
            generatePermissiveAterm();
        }
    }


    public void generateCompletionStrategies() throws IOException {
        writer.write("langspec/src-gen/completion/completion.str", "src-gen/completion/completion.str", true);
    }

    public void generateStratego2CompletionStrategies() throws IOException {
        writer.write("langspec/src-gen/completion/completion.str2", "src-gen/completion/completion.str2", true);
    }

    public void generatePermissiveAterm() throws IOException {
        // Add the predefined water rules for recovery, which will be normalized by SDF3
        // into src-gen/syntax/normalized/permissive-water-norm.sdf3
        writer.write("langspec/src-gen/syntax/permissive-water.sdf3", "src-gen/syntax/permissive-water.sdf3", true);
    }
}
