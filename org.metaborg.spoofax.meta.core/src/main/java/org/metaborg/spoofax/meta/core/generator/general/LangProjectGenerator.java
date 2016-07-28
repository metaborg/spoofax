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
public class LangProjectGenerator extends BaseGenerator {
    public LangProjectGenerator(GeneratorSettings settings, IFileAccess access) {
        super(settings, access);
    }

    public LangProjectGenerator(GeneratorSettings settings) {
        super(settings);
    }


    public static String siblingName(String id) {
        return id + ".example";
    }

    public static FileObject siblingDir(FileObject baseDir, String id) throws FileSystemException {
        return baseDir.resolveFile(siblingName(id));
    }


    public void generateAll() throws IOException {
        generatePOM();
        generateConfig();
    }


    public void generateConfig() throws IOException {
        writer.write("project/metaborg.yaml", "metaborg.yaml", false);
    }

    public void generatePOM() throws IOException {
        writer.write("project/pom.xml", "pom.xml", false);
    }
}
