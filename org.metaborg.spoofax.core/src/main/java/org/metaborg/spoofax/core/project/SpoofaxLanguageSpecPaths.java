package org.metaborg.spoofax.core.project;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.project.LanguageSpecPaths;
import org.metaborg.spoofax.core.project.configuration.ISpoofaxLanguageSpecConfig;

import java.io.IOException;
import java.io.ObjectInputStream;

import static org.metaborg.spoofax.core.SpoofaxConstants.*;

public class SpoofaxLanguageSpecPaths extends LanguageSpecPaths implements ISpoofaxLanguageSpecPaths {

    private static final long serialVersionUID = 5794344648827194188L;
    private final ISpoofaxLanguageSpecConfig config;

    public SpoofaxLanguageSpecPaths(FileObject rootFolder, ISpoofaxLanguageSpecConfig config) {
        super(rootFolder, config);
        this.config = config;
    }

    @Override
    public FileObject outputFolder() {
        return resolve(DIR_SRCGEN);
    }

    @Override
    public FileObject generatedSourceFolder() {
        return resolve(DIR_SRCGEN);
    }

    @Override
    public FileObject includeFolder() {
        return resolve(DIR_INCLUDE);
    }

    @Override
    public FileObject outputClassesFolder() {
        return resolve(DIR_CLASSES);
    }

    @Override
    public FileObject buildFolder() {
        return resolve(DIR_BUILD);
    }

    @Override
    public FileObject iconsFolder() {
        return resolve(DIR_ICONS);
    }

    @Override
    public FileObject libFolder() {
        return resolve(DIR_LIB);
    }

    @Override
    public FileObject syntaxFolder() {
        return resolve(DIR_SYNTAX);
    }

    @Override
    public FileObject editorFolder() {
        return resolve(DIR_EDITOR);
    }

    @Override
    public FileObject generatedSyntaxFolder() {
        return resolve(DIR_SRCGEN_SYNTAX);
    }

    @Override
    public FileObject transFolder() {
        return resolve(DIR_TRANS);
    }

    @Override
    public FileObject cacheFolder() {
        return resolve(DIR_CACHE);
    }

    @Override
    public FileObject mainEsvFile() {
        return resolve(DIR_EDITOR + "/" + this.config.name() + ".main.esv");
    }

    @Override
    public FileObject packedEsvFile() {
        return resolve(includeFolder(), this.config.esvName() + ".packed.esv");
    }

    @Override
    public FileObject strMainFile() {
        return resolve(transFolder(), this.config.strategoName() + ".str");
    }

    @Override
    public FileObject strJavaFolder() {
        return resolve(DIR_STR_JAVA);
    }

    @Override
    public FileObject strJavaPackageFolder() {
        return resolve(strJavaFolder(), packagePath());
    }

    @Override
    public FileObject strCompiledJavaPackageFolder() {
        return resolve(outputClassesFolder(), packagePath());
    }

    @Override
    public FileObject strJavaStrategiesFolder() {
        return resolve(strJavaPackageFolder(), "strategies");
    }

    @Override
    public FileObject strCompiledJavaStrategiesFolder() {
        return resolve(strCompiledJavaPackageFolder(), "strategies");
    }

    @Override
    public FileObject strJavaStrategiesMainFile() {
        return resolve(strJavaStrategiesFolder(), "Main.java");
    }

    @Override
    public FileObject strJavaTransFolder() {
        return resolve(DIR_STR_JAVA_TRANS);
    }

    @Override
    public FileObject strJavaMainFile() {
        return resolve(strJavaTransFolder(), "Main.java");
    }

    @Override
    public FileObject strCompiledJavaTransFolder() {
        return resolve(DIR_STR_JAVA_CLASSES);
    }

    @Override
    public FileObject strCompiledJarFile() {
        return resolve(includeFolder(), this.config.strategoName() + ".jar");
    }

    @Override
    public FileObject strCompiledJavaJarFile() {
        return resolve(includeFolder(), this.config.strategoName() + "-java.jar");
    }

    @Override
    public FileObject strCompiledCtreeFile() {
        return resolve(includeFolder(), this.config.strategoName() + ".ctree");
    }

    @Override
    public FileObject dsGeneratedInterpreterCompiledJavaFolder() {
        return resolve(outputClassesFolder(), "ds/generated/interpreter");
    }

    @Override
    public FileObject dsManualInterpreterCompiledJavaFolder() {
        return resolve(outputClassesFolder(), "ds/manual/interpreter");
    }


    @Override
    public String packagePath() {
        return this.config.packageName().replace('.', '/');
    }

    @Override
    public String strategiesPackagePath() {
        return this.config.strategiesPackageName().replace('.', '/');
    }




    @Override
    public String getSdfTableFilename(String sdfName) {
        return sdfName + ".tbl";
    }

    @Override
    public String getPpAfFilename(String sdfName) {
        return sdfName + ".pp.af";
    }

    @Override
    public String getGeneratedPpAfFilename(String sdfName) {
        return sdfName + ".generated.pp.af";
    }



    @Override
    public FileObject getSdfMainFile(String sdfName) {
        return resolve(generatedSyntaxFolder(), sdfName + ".sdf");
    }

    @Override
    public FileObject getSdfCompiledDefFile(String sdfName) {
        return resolve(includeFolder(), sdfName + ".def");
    }

    @Override
    public FileObject getSdfCompiledPermissiveDefFile(String sdfName) {
        return resolve(includeFolder(), sdfName + "-Permissive.def");
    }

    @Override
    public FileObject getSdfCompiledTableFile(String sdfName) {
        return resolve(includeFolder(), getSdfTableFilename(sdfName));
    }

    @Override
    public FileObject getRtgFile(String sdfName) {
        return resolve(includeFolder(), sdfName + ".rtg");
    }

    @Override
    public FileObject getStrCompiledParenthesizerFile(String sdfName) {
        return resolve(includeFolder(), sdfName + "-parenthesize.str");
    }

    @Override
    public FileObject getStrCompiledSigFile(String sdfName) {
        return resolve(includeFolder(), sdfName + ".str");
    }

    @Override
    public FileObject getPpFile(String sdfName) {
        return resolve(syntaxFolder(), sdfName + ".pp");
    }

    @Override
    public FileObject getGeneratedPpCompiledFile(String sdfName) {
        return resolve(includeFolder(), sdfName + ".generated.pp");
    }

    @Override
    public FileObject getPpAfCompiledFile(String sdfName) {
        return resolve(includeFolder(), getPpAfFilename(sdfName));
    }

    @Override
    public FileObject getGeneratedPpAfCompiledFile(String sdfName) {
        return resolve(includeFolder(), getGeneratedPpAfFilename(sdfName));
    }

}
