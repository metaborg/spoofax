package org.metaborg.spoofax.generator;

import java.io.IOException;

import org.metaborg.core.resource.IResourceService;
import org.metaborg.spoofax.generator.project.ProjectSettings;

public class ProjectGenerator extends BaseGenerator {
    public ProjectGenerator(IResourceService resourceService, ProjectSettings projectSettings) {
        super(resourceService, projectSettings);
    }


    public void generateAll() throws IOException {
        generateCommonLibrary();
        generateEditorServices();
    }

    public void generateCommonLibrary() throws IOException {
        writer.write("lib/editor-common.generated.str", true);
        writer.write("lib/refactor-common.generated.str", true);
    }

    public void generateEditorServices() throws IOException {
        writer.write("editor/{{name}}.generated.esv", true);
        for(String service : new String[] { "Builders", "Colorer", "Completions", "Folding", "Outliner",
            "Refactorings", "References", "Syntax" }) {
            writer.write(String.format("editor/{{name}}-%s.generated.esv", service),
                String.format("editor/{{name}}-%s.esv", service));
        }
        writer.write("editor/{{name}}-Outliner.generated.str", "editor/{{name}}-Outliner.str");
    }
}
