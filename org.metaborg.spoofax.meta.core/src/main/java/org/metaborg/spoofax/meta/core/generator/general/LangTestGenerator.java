package org.metaborg.spoofax.meta.core.generator.general;

import java.io.IOException;

import org.metaborg.spoofax.meta.core.generator.BaseGenerator;
import org.metaborg.spoofax.meta.core.generator.GeneratorSettings;
import org.metaborg.util.file.IFileAccess;

/**
 * Generates a language-specific project.
 */
public class LangTestGenerator extends BaseGenerator {
    public LangTestGenerator(GeneratorSettings settings, IFileAccess access) {
        super(settings, access);
    }

    public LangTestGenerator(GeneratorSettings settings) {
        super(settings);
    }


    public static String siblingName(String id) {
        return id + ".test";
    }


    public void generateAll() throws IOException {
        generateConfig();
        generatePOM();
        generateTest();
        generateIgnoreFile();
    }


    public void generateConfig() throws IOException {
        writer.write("test/metaborg.yaml", "metaborg.yaml", false);
    }

    public void generatePOM() throws IOException {
        writer.write("test/pom.xml", "pom.xml", false);
    }

    public void generateTest() throws IOException {
        writer.write("test/test.spt", "test.spt", false);
    }

    public void generateIgnoreFile() throws IOException {
        writer.write("test/vcsignore", ".gitignore", false);
    }
}
