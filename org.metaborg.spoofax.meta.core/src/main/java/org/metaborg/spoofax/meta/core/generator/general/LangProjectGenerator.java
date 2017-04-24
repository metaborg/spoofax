package org.metaborg.spoofax.meta.core.generator.general;

import java.io.IOException;

import org.metaborg.spoofax.meta.core.generator.BaseGenerator;
import org.metaborg.spoofax.meta.core.generator.GeneratorSettings;
import org.metaborg.util.file.IFileAccess;

/**
 * Generates a language-specific project.
 */
public class LangProjectGenerator extends BaseGenerator {
    private final GeneratorSettings settings;

    public LangProjectGenerator(GeneratorSettings settings, IFileAccess access) {
        super(settings, access);
        this.settings = settings;
    }

    public LangProjectGenerator(GeneratorSettings settings) {
        super(settings);
        this.settings = settings;
    }


    public static String siblingName(String id) {
        return id + ".example";
    }


    public void generateAll() throws IOException {
        generatePOM();
        generateConfig();
        generateIgnoreFile();
    }


    public void generateConfig() throws IOException {
        writer.write("project/metaborg.yaml", "metaborg.yaml", false);
    }

    public void generatePOM() throws IOException {
        writer.write("project/pom.xml", "pom.xml", false);
    }

    public void generateIgnoreFile() throws IOException {
        writer.write("project/vcsignore", ".gitignore", false);
    }

    public boolean analysisNabl2() {
        return settings.analysisType() == AnalysisType.NaBL2;
    }
}
