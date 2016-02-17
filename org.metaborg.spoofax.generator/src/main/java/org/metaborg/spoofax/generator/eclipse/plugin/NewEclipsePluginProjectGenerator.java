package org.metaborg.spoofax.generator.eclipse.plugin;

import java.io.File;
import java.io.IOException;

import org.metaborg.spoofax.generator.BaseGenerator;
import org.metaborg.spoofax.generator.IGeneratorSettings;
import org.metaborg.util.file.FileAccess;

/**
 * Generates a new Eclipse plugin companion project for a language project, that lifts a language project into an
 * Eclipse plugin that can be installed into Eclipse.
 */
public class NewEclipsePluginProjectGenerator extends BaseGenerator {
    public NewEclipsePluginProjectGenerator(IGeneratorSettings scope, FileAccess access) {
        super(scope, access);
    }
    
    public NewEclipsePluginProjectGenerator(IGeneratorSettings scope) {
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
        writer.write(".project", false);
    }

    public void generateClasspath() throws IOException {
        writer.write(".classpath", false);
    }

    public void generatePOM() throws IOException {
        writer.write("pom.xml", false);
    }

    public void generateManifest() throws IOException {
        writer.write("META-INF/MANIFEST.MF", false);
    }

    public void generatePluginXML() throws IOException {
        writer.write("plugin.xml", false);
    }

    public void generateBuildProperties() throws IOException {
        writer.write("build.properties", false);
    }

    public void generateIgnoreFile() throws IOException {
        writer.write("vcsignore", ".gitignore", false);
    }
}
