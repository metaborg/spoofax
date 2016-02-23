package org.metaborg.spoofax.meta.core.project;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.meta.core.project.ILanguageSpecPaths;

/**
 * Specifies the filenames, files, and paths used in a Spoofax language specification.
 */
public interface ISpoofaxLanguageSpecPaths extends ILanguageSpecPaths {
    // NOTE: Name for getter for
    // - filename String     ends with "Filename";
    // - path     String     ends with "Path";
    // - file     FileObject ends with "File";
    // - folder   FileObject ends with "Folder".

    /**
     * Gets the folder for generated source files.
     *
     * @return A folder {@link FileObject}.
     */
    FileObject generatedSourceFolder();

    /**
     * Gets the include folder.
     *
     * @return A folder {@link FileObject}.
     */
    FileObject includeFolder();

    /**
     * Gets the folder for classes.
     *
     * @return A folder {@link FileObject}.
     */
    FileObject outputClassesFolder();

    /**
     * Gets the build folder.
     *
     * @return A folder {@link FileObject}.
     */
    FileObject buildFolder();

    /**
     * Gets the icons folder.
     *
     * @return A folder {@link FileObject}.
     */
    FileObject iconsFolder() ;

    /**
     * Gets the lib folder.
     *
     * @return A folder {@link FileObject}.
     */
    FileObject libFolder();

    /**
     * Gets the syntax folder.
     *
     * @return A folder {@link FileObject}.
     */
    FileObject syntaxFolder();

    /**
     * Gets the editor folder.
     *
     * @return A folder {@link FileObject}.
     */
    FileObject editorFolder();

    /**
     * Gets the generated syntax folder.
     *
     * @return A folder {@link FileObject}.
     */
    FileObject generatedSyntaxFolder();

    /**
     * Gets the trans folder.
     *
     * @return A folder {@link FileObject}.
     */
    FileObject transFolder();

    /**
     * Gets the cache folder.
     *
     * @return A folder {@link FileObject}.
     */
    FileObject cacheFolder();

    /**
     * Gets the main ESV file.
     *
     * @return A file {@link FileObject}.
     */
    FileObject mainEsvFile();

    /**
     * Gets the packed ESV file.
     *
     * @return A file {@link FileObject}.
     */
    FileObject packedEsvFile();

    FileObject strMainFile();

    FileObject strJavaFolder();

    FileObject strJavaPackageFolder();

    FileObject strCompiledJavaPackageFolder();

    FileObject strJavaStrategiesFolder();

    FileObject strCompiledJavaStrategiesFolder();

    FileObject strJavaStrategiesMainFile();

    FileObject strJavaTransFolder();

    FileObject strJavaMainFile();

    FileObject strCompiledJavaTransFolder();

    FileObject strCompiledJarFile();

    FileObject strCompiledJavaJarFile();

    FileObject strCompiledCtreeFile();

    FileObject dsMainFile();

    FileObject dsGeneratedInterpreterJava();

    FileObject dsGeneratedInterpreterCompiledJavaFolder();

    FileObject dsManualInterpreterCompiledJavaFolder();

    /**
     * Gets the SDF table filename.
     *
     * @param sdfName The SDF name.
     * @return The SDF table filename.
     */
    String getSdfTableFilename(String sdfName);

    /**
     * Gets the pp.af filename.
     *
     * @param sdfName The SDF name.
     * @return The pp.af filename.
     */
    String getPpAfFilename(String sdfName);

    /**
     * Gets the generated pp.af filename.
     *
     * @param sdfName The SDF name.
     * @return The generated pp.af filename.
     */
    String getGeneratedPpAfFilename(String sdfName);

    FileObject getSdfMainFile(String sdfName);

    FileObject getSdfCompiledDefFile(String sdfName);

    FileObject getSdfCompiledPermissiveDefFile(String sdfName);

    FileObject getSdfCompiledTableFile(String sdfName);

    FileObject getRtgFile(String sdfName);

    FileObject getStrCompiledParenthesizerFile(String sdfName);

    FileObject getStrCompiledSigFile(String sdfName);

    FileObject getPpFile(String sdfName);

    FileObject getGeneratedPpCompiledFile(String sdfName);

    FileObject getPpAfCompiledFile(String sdfName);

    FileObject getGeneratedPpAfCompiledFile(String sdfName);


    /**
     * Gets the package path.
     *
     * @return The package path.
     */
    String packagePath();

    /**
     * Gets the strategies package path.
     *
     * @return The strategies package path.
     */
    String strategiesPackagePath();
}
