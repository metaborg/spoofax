package org.metaborg.spoofax.meta.core.generator.language;

import java.io.IOException;

import org.metaborg.spoofax.meta.core.generator.BaseGenerator;
import org.metaborg.spoofax.meta.core.generator.GeneratorSettings;
import org.metaborg.util.file.FileAccess;

/**
 * Generates project files which need to be generated after each build. Files are not specific to an IDE.
 */
public class ContinuousLanguageSpecGenerator extends BaseGenerator {
    public ContinuousLanguageSpecGenerator(GeneratorSettings scope, FileAccess access) {
        super(scope, access);
    }

    public ContinuousLanguageSpecGenerator(GeneratorSettings scope) {
        super(scope);
    }

    public void generateAll() throws IOException {
        generateCommonLibrary();
        generateEditorServices();
    }


    public void generateCommonLibrary() throws IOException {
        writer.write("lib/editor-common.generated.str", true);
    }

    public void generateEditorServices() throws IOException {
        writer.write("src-gen/editor/Colorer.generated.esv", true);
    }
}
