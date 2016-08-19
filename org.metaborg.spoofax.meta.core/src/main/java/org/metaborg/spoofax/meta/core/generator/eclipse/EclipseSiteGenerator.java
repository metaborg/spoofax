package org.metaborg.spoofax.meta.core.generator.eclipse;

import java.io.IOException;

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
