package org.metaborg.spoofax.generator.eclipse;

import java.io.IOException;
import org.metaborg.spoofax.generator.BaseGenerator;
import org.metaborg.spoofax.generator.project.ProjectSettings;

public class EclipseProjectGenerator extends BaseGenerator {

    public EclipseProjectGenerator(ProjectSettings projectSettings) {
        super(projectSettings);
    }

    public void generateAll() throws IOException {
        writer.write("editor/java/{{packagePath}}/Activator.java", false);
        writer.write("editor/java/{{packagePath}}/{{javaName}}ParseController.java", false);
        writer.write("editor/java/{{packagePath}}/{{javaName}}ParseControllerGenerated.java", true);
        writer.write("editor/java/{{packagePath}}/{{javaName}}Validator.java", true);
    }

}
