package org.metaborg.spoofax.generator.eclipse.language;

import java.io.IOException;

import org.metaborg.spoofax.generator.BaseGenerator;
import org.metaborg.spoofax.generator.project.GeneratorProjectSettings;

/**
 * Generates Eclipse support files for language projects.
 */
public class EclipseProjectGenerator extends BaseGenerator {
    public EclipseProjectGenerator(GeneratorProjectSettings settings) {
        super(settings);
    }


    public void generateAll() throws IOException {
        generateClasspath();
    }


    public void generateClasspath() throws IOException {
        writer.write(".classpath", false);
    }
}
