package org.metaborg.spoofax.generator.eclipse.language;

import org.metaborg.spoofax.generator.BaseGenerator;
import org.metaborg.spoofax.generator.NewBaseGenerator;
import org.metaborg.spoofax.generator.project.GeneratorProjectSettings;
import org.metaborg.spoofax.generator.project.LanguageSpecGeneratorScope;

import java.io.IOException;

/**
 * Generates Eclipse support files for language projects.
 */
public class NewEclipseProjectGenerator extends NewBaseGenerator {
    public NewEclipseProjectGenerator(LanguageSpecGeneratorScope scope) {
        super(scope);
    }


    public void generateAll() throws IOException {
        generateClasspath();
    }


    public void generateClasspath() throws IOException {
        writer.write(".classpath", false);
    }
}
