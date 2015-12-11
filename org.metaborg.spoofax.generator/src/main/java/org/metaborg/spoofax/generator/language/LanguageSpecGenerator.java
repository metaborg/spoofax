package org.metaborg.spoofax.generator.language;

import org.metaborg.spoofax.generator.BaseGenerator;
import org.metaborg.spoofax.generator.NewBaseGenerator;
import org.metaborg.spoofax.generator.project.GeneratorProjectSettings;
import org.metaborg.spoofax.generator.project.LanguageSpecGeneratorScope;

import java.io.IOException;

/**
 * Generates project files which need to be generated after each build. Files are not specific to an IDE.
 */
public class LanguageSpecGenerator extends NewBaseGenerator {
    public LanguageSpecGenerator(LanguageSpecGeneratorScope scope) {
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
