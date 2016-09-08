package org.metaborg.spoofax.meta.core.generator.general;

import java.io.IOException;

import org.metaborg.spoofax.meta.core.config.SdfVersion;
import org.metaborg.spoofax.meta.core.generator.BaseGenerator;
import org.metaborg.spoofax.meta.core.generator.GeneratorSettings;
import org.metaborg.util.file.IFileAccess;

/**
 * Generates project files which need to be generated after each build. Files are not specific to an IDE.
 */
public class ContinuousLanguageSpecGenerator extends BaseGenerator {
    SdfVersion version;
    
    public ContinuousLanguageSpecGenerator(GeneratorSettings scope, IFileAccess access, SdfVersion version) {
        super(scope, access);
        this.version = version;
    }

    public ContinuousLanguageSpecGenerator(GeneratorSettings scope, SdfVersion version) {
        super(scope);
        this.version = version;
    }

    public void generateAll() throws IOException {
        generateMetaborgLibrary();
        generateEditorServices();
        if(version == SdfVersion.sdf3) {
            generateCompletionStrategies();
        }
    }


    public void generateMetaborgLibrary() throws IOException {
        writer.write("langspec/src-gen/stratego/metaborg.str", "src-gen/stratego/metaborg.str", true);
    }

    public void generateCompletionStrategies() throws IOException {
        writer.write("langspec/src-gen/completion/completion.str", "src-gen/completion/completion.str", true);
    }

    public void generateEditorServices() throws IOException {
        writer.write("langspec/src-gen/editor/Colorer.generated.esv", "src-gen/editor/Colorer.generated.esv", true);
    }
}
