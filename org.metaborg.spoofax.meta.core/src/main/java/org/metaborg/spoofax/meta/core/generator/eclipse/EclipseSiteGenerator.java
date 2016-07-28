package org.metaborg.spoofax.meta.core.generator.eclipse;

import java.io.IOException;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.metaborg.spoofax.meta.core.generator.BaseGenerator;
import org.metaborg.spoofax.meta.core.generator.GeneratorSettings;
import org.metaborg.util.file.IFileAccess;

public class EclipseSiteGenerator extends BaseGenerator {
    public EclipseSiteGenerator(GeneratorSettings scope, IFileAccess access) {
        super(scope, access);
    }

    public EclipseSiteGenerator(GeneratorSettings scope) {
        super(scope);
    }


    public static String siblingName(String id) {
        return id + ".eclipse.site";
    }

    public static FileObject siblingDir(FileObject baseDir, String id) throws FileSystemException {
        return baseDir.resolveFile(siblingName(id));
    }


    public void generateAll() throws IOException {
        generatePOM();
        generateUpdatesiteXML();
        generateIgnoreFile();
    }


    public void generatePOM() throws IOException {
        writer.write("site/pom.xml", "pom.xml", false);
    }

    public void generateUpdatesiteXML() throws IOException {
        writer.write("site/site.xml", "site.xml", false);
    }

    public void generateIgnoreFile() throws IOException {
        writer.write("site/vcsignore", ".gitignore", false);
    }
}
