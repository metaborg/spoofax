package org.metaborg.spoofax.generator;

import java.io.File;
import java.io.IOException;
import org.metaborg.spoofax.generator.BaseGenerator;

public class ProjectGenerator extends BaseGenerator {

    public ProjectGenerator(File root, String sdfMainModule) {
        super(root, sdfMainModule);
    }
    
    public void generateAll() throws IOException {
        generateCommonLibrary();
        generateRuntimeLibrary();
        generateEditorServices();
    }

    private void generateCommonLibrary() throws IOException {
        writer.write("lib/editor-common.generated.str", true);
        writer.write("lib/refactor-common.generated.str", true);
    }

    private void generateRuntimeLibrary() throws IOException {
        unpack("lib/runtime.zip");
    }

    public void generateEditorServices() throws IOException {
        writer.write("editor/{{sdfMainModule}}-Colorer.generated.esv", true);
        writer.write("editor/{{sdfMainModule}}-Completions.generated.esv", true);
        writer.write("editor/{{sdfMainModule}}-Folding.generated.esv", true);
        writer.write("editor/{{sdfMainModule}}-Outliner.generated.str", true);
        writer.write("editor/{{sdfMainModule}}-Refactorings.generated.esv", true);
        writer.write("editor/{{sdfMainModule}}-References.generated.esv", true);
        writer.write("editor/{{sdfMainModule}}-Syntax.generated.esv", true);
    }

}
