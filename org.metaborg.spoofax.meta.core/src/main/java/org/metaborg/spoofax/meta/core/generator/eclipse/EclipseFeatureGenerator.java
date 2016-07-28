package org.metaborg.spoofax.meta.core.generator.eclipse;

import java.io.IOException;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.metaborg.spoofax.meta.core.generator.BaseGenerator;
import org.metaborg.spoofax.meta.core.generator.GeneratorSettings;
import org.metaborg.util.file.IFileAccess;

public class EclipseFeatureGenerator extends BaseGenerator {
    public EclipseFeatureGenerator(GeneratorSettings scope, IFileAccess access) {
        super(scope, access);
    }

    public EclipseFeatureGenerator(GeneratorSettings scope) {
        super(scope);
    }


    public static String siblingName(String id) {
        return id + ".eclipse.feature";
    }

    public static FileObject siblingDir(FileObject baseDir, String id) throws FileSystemException {
        return baseDir.resolveFile(siblingName(id));
    }


    public void generateAll() throws IOException {
        generatePOM();
        generateFeatureXML();
        generateBuildProperties();
        generateIgnoreFile();
    }


    public void generatePOM() throws IOException {
        writer.write("feature/pom.xml", "pom.xml", false);
    }

    public void generateFeatureXML() throws IOException {
        writer.write("feature/feature.xml", "feature.xml", false);
    }

    public void generateBuildProperties() throws IOException {
        writer.write("feature/build.properties", "build.properties", false);
    }

    public void generateIgnoreFile() throws IOException {
        writer.write("feature/vcsignore", ".gitignore", false);
    }
}
