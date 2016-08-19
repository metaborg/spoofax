package org.metaborg.spoofax.meta.core.generator.general;

import java.io.IOException;

import org.metaborg.spoofax.meta.core.generator.BaseGenerator;
import org.metaborg.spoofax.meta.core.generator.GeneratorSettings;
import org.metaborg.util.file.IFileAccess;

/**
 * Generates project files which need to be generated after each build. Files are not specific to an IDE.
 */
public class ContinuousLanguageSpecGenerator extends BaseGenerator {
    public ContinuousLanguageSpecGenerator(GeneratorSettings scope, IFileAccess access) {
        super(scope, access);
    }

    public ContinuousLanguageSpecGenerator(GeneratorSettings scope) {
        super(scope);
    }

    public void generateAll() throws IOException {
        generateMetaborgLibrary();
        generateEditorServices();
    }


    public void generateMetaborgLibrary() throws IOException {
        writer.write("langspec/src-gen/stratego/metaborg.str", "src-gen/stratego/metaborg.str", true);
    }

    public void generateEditorServices() throws IOException {
        writer.write("langspec/src-gen/editor/Colorer.generated.esv", "src-gen/editor/Colorer.generated.esv", true);
    }
}
