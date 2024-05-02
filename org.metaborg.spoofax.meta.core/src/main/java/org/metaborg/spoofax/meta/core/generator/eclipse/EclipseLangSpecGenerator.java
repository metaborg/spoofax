package org.metaborg.spoofax.meta.core.generator.eclipse;

import java.io.IOException;

import org.metaborg.spoofax.meta.core.generator.BaseGenerator;
import org.metaborg.spoofax.meta.core.generator.GeneratorSettings;
import mb.util.vfs2.file.IFileAccess;

/**
 * Generates Eclipse support files for language specification projects.
 */
public class EclipseLangSpecGenerator extends BaseGenerator {
    public EclipseLangSpecGenerator(GeneratorSettings settings, IFileAccess access) {
        super(settings, access);
    }

    public EclipseLangSpecGenerator(GeneratorSettings settings) {
        super(settings);
    }


    public void generateAll() throws IOException {
        generateClasspath();
    }


    public void generateClasspath() throws IOException {
        writer.write("langspec/.classpath", ".classpath", false);
    }
}
