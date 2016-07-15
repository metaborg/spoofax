package org.metaborg.spoofax.meta.core.generator.eclipse;

import java.io.File;
import java.io.IOException;

import org.metaborg.spoofax.meta.core.generator.BaseGenerator;
import org.metaborg.spoofax.meta.core.generator.GeneratorSettings;
import org.metaborg.util.file.IFileAccess;

/**
 * Generates a companion Eclipse plugin project for a language specification project, that lifts the language
 * implementation built from the language specification project into an Eclipse plugin that can be installed into
 * Eclipse.
 */
public class EclipsePluginGenerator extends BaseGenerator {
    public EclipsePluginGenerator(GeneratorSettings scope, IFileAccess access) {
        super(scope, access);
    }

    public EclipsePluginGenerator(GeneratorSettings scope) {
        super(scope);
    }


    public static File childBaseDir(File baseDir, String id) {
        return new File(baseDir, id + ".eclipse");
    }


    public void generateAll() throws IOException {
        generateProject();
        generateClasspath();
        generatePOM();
        generateManifest();
        generatePluginXML();
        generateBuildProperties();
        generateIgnoreFile();
    }


    public void generateProject() throws IOException {
        writer.write("plugin/.project", ".project", false);
    }

    public void generateClasspath() throws IOException {
        writer.write("plugin/.classpath", ".classpath", false);
    }

    public void generatePOM() throws IOException {
        writer.write("plugin/pom.xml", "pom.xml", false);
    }

    public void generateManifest() throws IOException {
        writer.write("plugin/META-INF/MANIFEST.MF", "META-INF/MANIFEST.MF", false);
    }

    public void generatePluginXML() throws IOException {
        writer.write("plugin/plugin.xml", "plugin.xml", false);
    }

    public void generateBuildProperties() throws IOException {
        writer.write("plugin/build.properties", "build.properties", false);
    }

    public void generateIgnoreFile() throws IOException {
        writer.write("plugin/vcsignore", ".gitignore", false);
    }
}
