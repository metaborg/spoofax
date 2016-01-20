package org.metaborg.spoofax.core.project;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.project.configuration.ILanguageSpecConfig;
import org.metaborg.core.resource.IResourceService;
import org.metaborg.spoofax.core.project.settings.SpoofaxProjectSettings;

import java.net.URI;

import static org.metaborg.spoofax.core.SpoofaxConstants.DIR_STR_JAVA;

/**
 * This class is used to temporarily bridge between the old and new configuration systems.
 */
@SuppressWarnings("deprecation")
public class LegacySpoofaxLanguageSpecPaths implements ISpoofaxLanguageSpecPaths {
    private static final long serialVersionUID = -1830431450114231566L;

    private final SpoofaxProjectSettings settings;

    public LegacySpoofaxLanguageSpecPaths(final SpoofaxProjectSettings settings) {
        this.settings = settings;
    }

    @Override
    public FileObject generatedSourceFolder() {
        return this.settings.getGenSourceDirectory();
    }

    @Override
    public FileObject includeFolder() {
        return this.settings.getIncludeDirectory();
    }

    @Override
    public FileObject outputClassesFolder() {
        return this.settings.getOutputClassesDirectory();
    }

    @Override
    public FileObject buildFolder() {
        return this.settings.getBuildDirectory();
    }

    @Override
    public FileObject iconsFolder() {
        return this.settings.getIconsDirectory();
    }

    @Override
    public FileObject libFolder() {
        return this.settings.getLibDirectory();
    }

    @Override
    public FileObject syntaxFolder() {
        return this.settings.getSyntaxDirectory();
    }

    @Override
    public FileObject editorFolder() {
        return this.settings.getEditorDirectory();
    }

    @Override
    public FileObject generatedSyntaxFolder() {
        return this.settings.getGenSyntaxDirectory();
    }

    @Override
    public FileObject transFolder() {
        return this.settings.getTransDirectory();
    }

    @Override
    public FileObject cacheFolder() {
        return this.settings.getCacheDirectory();
    }

    @Override
    public FileObject mainEsvFile() {
        return this.settings.getMainESVFile();
    }

    @Override
    public FileObject packedEsvFile() {
        return this.settings.getPackedEsv();
    }

    @Override
    public FileObject strMainFile() {
        return this.settings.getStrMainFile();
    }

    @Override
    public FileObject strJavaFolder() {
        return this.settings.getStrJavaDirectory();
    }

    @Override
    public FileObject strJavaPackageFolder() {
        return this.settings.getStrJavaPackageDirectory();
    }

    @Override
    public FileObject strCompiledJavaPackageFolder() {
        return this.settings.getStrCompiledJavaPackageDirectory();
    }

    @Override
    public FileObject strJavaStrategiesFolder() {
        return this.settings.getStrJavaStrategiesDirectory();
    }

    @Override
    public FileObject strCompiledJavaStrategiesFolder() {
        return this.settings.getStrCompiledJavaStrategiesDirectory();
    }

    @Override
    public FileObject strJavaStrategiesMainFile() {
        return this.settings.getStrJavaStrategiesMainFile();
    }

    @Override
    public FileObject strJavaTransFolder() {
        return this.settings.getStrJavaTransDirectory();
    }

    @Override
    public FileObject strJavaMainFile() {
        return this.settings.getStrJavaMainFile();
    }

    @Override
    public FileObject strCompiledJavaTransFolder() {
        return this.settings.getStrCompiledJavaTransDirectory();
    }

    @Override
    public FileObject strCompiledJarFile() {
        return this.settings.getStrCompiledJarFile();
    }

    @Override
    public FileObject strCompiledJavaJarFile() {
        return this.settings.getStrCompiledJavaJarFile();
    }

    @Override
    public FileObject strCompiledCtreeFile() {
        return this.settings.getStrCompiledCtreeFile();
    }

    @Override
    public FileObject dsMainFile() {
        return this.settings.getMainDsFile();
    }

    @Override
    public FileObject dsGeneratedInterpreterJava() {
        return this.settings.getDsGeneratedInterpreterJava();
    }

    @Override
    public FileObject dsGeneratedInterpreterCompiledJavaFolder() {
        return this.settings.getDsGeneratedInterpreterCompiledJava();
    }

    @Override
    public FileObject dsManualInterpreterCompiledJavaFolder() {
        return this.settings.getDsManualInterpreterCompiledJava();
    }

    @Override
    public String getSdfTableFilename(String sdfName) {
        return this.settings.getSdfTableName(sdfName);
    }

    @Override
    public String getPpAfFilename(String sdfName) {
        return this.settings.getPpAfName(sdfName);
    }

    @Override
    public String getGeneratedPpAfFilename(String sdfName) {
        return this.settings.getGenPpAfName(sdfName);
    }

    @Override
    public FileObject getSdfMainFile(String sdfName) {
        return this.settings.getSdfMainFile(sdfName);
    }

    @Override
    public FileObject getSdfCompiledDefFile(String sdfName) {
        return this.settings.getSdfCompiledDefFile(sdfName);
    }

    @Override
    public FileObject getSdfCompiledPermissiveDefFile(String sdfName) {
        return this.settings.getSdfCompiledPermissiveDefFile(sdfName);
    }

    @Override
    public FileObject getSdfCompiledTableFile(String sdfName) {
        return this.settings.getSdfCompiledTableFile(sdfName);
    }

    @Override
    public FileObject getRtgFile(String sdfName) {
        return this.settings.getRtgFile(sdfName);
    }

    @Override
    public FileObject getStrCompiledParenthesizerFile(String sdfName) {
        return this.settings.getStrCompiledParenthesizerFile(sdfName);
    }

    @Override
    public FileObject getStrCompiledSigFile(String sdfName) {
        return this.settings.getStrCompiledSigFile(sdfName);
    }

    @Override
    public FileObject getPpFile(String sdfName) {
        return this.settings.getPpFile(sdfName);
    }

    @Override
    public FileObject getGeneratedPpCompiledFile(String sdfName) {
        return this.settings.getGenPpCompiledFile(sdfName);
    }

    @Override
    public FileObject getPpAfCompiledFile(String sdfName) {
        return this.settings.getPpAfCompiledFile(sdfName);
    }

    @Override
    public FileObject getGeneratedPpAfCompiledFile(String sdfName) {
        return this.settings.getGenPpAfCompiledFile(sdfName);
    }

    @Override
    public String packagePath() {
        return this.settings.packagePath();
    }

    @Override
    public String strategiesPackagePath() {
        return this.settings.packageStrategiesPath();
    }

    @Override
    public FileObject rootFolder() {
        return this.settings.location();
    }

    @Override
    public FileObject outputFolder() {
        return this.settings.getOutputDirectory();
    }

    @Override
    public void initAfterDeserialization(IResourceService resourceService) {
        this.settings.initAfterDeserialization(resourceService);
    }
}
