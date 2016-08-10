package org.metaborg.spoofax.meta.core.generator.general;

import java.io.IOException;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
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

    public static FileObject siblingDir(FileObject baseDir, String id) throws FileSystemException {
        return baseDir.resolveFile(siblingName(id));
    }


    public void generateAll() throws IOException {
        generateConfig();
        generatePOM();
        generateTest();
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
}
