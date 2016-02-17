package org.metaborg.spoofax.generator.language;

import java.io.IOException;

import org.metaborg.spoofax.generator.BaseGenerator;
import org.metaborg.spoofax.generator.IGeneratorSettings;
import org.metaborg.util.file.FileAccess;

/**
 * Generates project files which need to be generated after each build. Files are not specific to an IDE.
 */
public class LanguageSpecGenerator extends BaseGenerator {
    public LanguageSpecGenerator(IGeneratorSettings scope, FileAccess access) {
        super(scope, access);
    }

    public LanguageSpecGenerator(IGeneratorSettings scope) {
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
