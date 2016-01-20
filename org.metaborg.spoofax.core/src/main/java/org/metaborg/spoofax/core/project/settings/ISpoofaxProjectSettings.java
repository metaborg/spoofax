package org.metaborg.spoofax.core.project.settings;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.project.settings.IProjectSettings;
import org.metaborg.spoofax.core.project.configuration.ISpoofaxLanguageSpecConfig;

/**
 * Spoofax-specific settings for a project.
 *
 * @deprecated Use {@link ISpoofaxLanguageSpecConfig} instead.
 */
@SuppressWarnings("deprecation")
@Deprecated
public interface ISpoofaxProjectSettings extends IProjectSettings {

    /**
     * Gets the location of the project root.
     *
     * @return The project root location.
     */
    FileObject location();

    /**
     * Gets a sequence of languages whose errors are ignored.
     *
     * @return An iterable of pardoned languages.
     */
    Iterable<String> pardonedLanguages();

    /**
     * Gets the project artifact format.
     *
     * @return A member of the {@link Format} enumeration.
     */
    Format format();

    /**
     * Gets SDF arguments.
     *
     * @return An iterable of SDF arguments.
     */
    Iterable<String> sdfArgs();

    /**
     * Gets the Stratego arguments.
     *
     * @return The stratego arguments.
     */
    Iterable<String> strategoArgs();

    /**
     * Gets the external def.
     *
     * @return The external def.
     */
    String externalDef();

    /**
     * Gets the external JAR.
     *
     * @return The external JAR.
     */
    String externalJar();

    /**
     * Gets the external JAR flags.
     *
     * @return The external JAR flags.
     */
    String externalJarFlags();

    /**
     * Gets the Stratego name.
     *
     * @return The Stratego name.
     */
    String strategoName();

    /**
     * Gets the Java name.
     *
     * @return The Java name.
     */
    String javaName();

    /**
     * Gets the package name.
     *
     * @return The package name.
     */
    String packageName();

    /**
     * Gets the package path.
     *
     * @return The package path.
     */
    String packagePath();

    /**
     * Gets the generated source directory.
     *
     * @return The generated source directory.
     */
    FileObject getGeneratedSourceDirectory();

    /**
     * Gets the output directory.
     *
     * @return The output directory.
     */
    FileObject getOutputDirectory();

    /**
     * Gets the icons directory.
     *
     * @return The icons directory.
     */
    FileObject getIconsDirectory();

    /**
     * Gets the lib directory.
     *
     * @return The lib directory.
     */
    FileObject getLibDirectory();

    /**
     * Gets the syntax directory.
     *
     * @return The syntax directory.
     */
    FileObject getSyntaxDirectory();

    /**
     * Gets the editor directory.
     *
     * @return The editor directory.
     */
    FileObject getEditorDirectory();

    /**
     * Gets the Java directory.
     *
     * @return The Java directory.
     */
    FileObject getJavaDirectory();

    /**
     * Gets the Java trans directory.
     *
     * @return The Java trans directory.
     */
    FileObject getJavaTransDirectory();

    /**
     * Gets the generated syntax directory.
     *
     * @return The generated syntax directory.
     */
    FileObject getGeneratedSyntaxDirectory();

    /**
     * Gets the trans directory.
     *
     * @return The trans directory.
     */
    FileObject getTransDirectory();

    /**
     * Gets the cache directory.
     *
     * @return The cache directory.
     */
    FileObject getCacheDirectory();

    /**
     * Gets the main ESV file.
     *
     * @return The main ESV file.
     */
    FileObject getMainESVFile();
}
