package org.metaborg.spoofax.generator.eclipse.language;

import java.io.IOException;

import org.metaborg.spoofax.generator.NewBaseGenerator;
import org.metaborg.spoofax.generator.project.LanguageSpecGeneratorScope;
import org.metaborg.util.file.FileAccess;

/**
 * Generates Eclipse support files for language projects.
 *
 * @deprecated To remove.
 */
@Deprecated
public class NewEclipseProjectGenerator extends NewBaseGenerator {
    public NewEclipseProjectGenerator(LanguageSpecGeneratorScope scope, FileAccess access) {
        super(scope, access);
    }


    public void generateAll() throws IOException {
        generateClasspath();
    }


    public void generateClasspath() throws IOException {
        writer.write(".classpath", false);
    }
}
