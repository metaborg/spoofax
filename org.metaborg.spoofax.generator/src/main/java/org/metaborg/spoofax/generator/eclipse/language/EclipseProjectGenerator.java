package org.metaborg.spoofax.generator.eclipse.language;

import java.io.IOException;

import org.metaborg.spoofax.generator.NewBaseGenerator;
import org.metaborg.spoofax.generator.project.LanguageSpecGeneratorScope;
import org.metaborg.util.file.FileAccess;

/**
 * Generates Eclipse support files for language projects.
 */
public class EclipseProjectGenerator extends NewBaseGenerator {
    public EclipseProjectGenerator(LanguageSpecGeneratorScope settings, FileAccess access) {
        super(settings, access);
    }
    
    public EclipseProjectGenerator(LanguageSpecGeneratorScope settings) {
        super(settings);
    }


    public void generateAll() throws IOException {
        generateClasspath();
    }


    public void generateClasspath() throws IOException {
        writer.write(".classpath", false);
    }
}
