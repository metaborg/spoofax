package org.metaborg.spoofax.generator.eclipse;

import java.io.IOException;
import org.apache.commons.lang3.StringUtils;
import org.metaborg.spoofax.generator.BaseGenerator;
import org.metaborg.spoofax.generator.project.ProjectSettings;

public class NewEclipseProjectGenerator extends BaseGenerator {

    private final String[] fileExtensions;

    public NewEclipseProjectGenerator(ProjectSettings projectSettings,
            String[] fileExtensions) {
        super(projectSettings);
        this.fileExtensions = fileExtensions;
    }

    public String fileExtensions() {
        return StringUtils.join(fileExtensions, ", ");
    }

    public void generateAll() throws IOException {
        generatePOM();
        generateManifest();
        generatePluginXML();
        generateBuildProperties();
        generateIgnoreFile();
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
