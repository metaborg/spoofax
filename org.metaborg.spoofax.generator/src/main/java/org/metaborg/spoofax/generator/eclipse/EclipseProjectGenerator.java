package org.metaborg.spoofax.generator.eclipse;

import java.io.IOException;

import org.metaborg.spoofax.generator.BaseGenerator;
import org.metaborg.spoofax.generator.project.ProjectSettings;

public class EclipseProjectGenerator extends BaseGenerator {
    public EclipseProjectGenerator(ProjectSettings projectSettings) {
        super(projectSettings);
    }


    public void generateAll() throws IOException {
        generateProject();
        generatePOM();
        generateManifest();
        generatePluginXML();
        generateBuildProperties();
        generateIgnoreFile();
    }

    public void generateProject() throws IOException {
        writer.write(".project", false);
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
