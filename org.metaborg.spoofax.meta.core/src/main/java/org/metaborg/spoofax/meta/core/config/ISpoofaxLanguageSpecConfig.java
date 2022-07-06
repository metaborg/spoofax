package org.metaborg.spoofax.meta.core.config;

import java.util.Collection;
import java.util.List;

import javax.annotation.Nullable;

import org.metaborg.meta.core.config.ILanguageSpecConfig;
import org.metaborg.meta.core.config.ILanguageSpecConfigBuilder;
import org.metaborg.spoofax.core.config.ISpoofaxProjectConfig;
import org.metaborg.util.cmd.Arguments;

/**
 * Spoofax-specific configuration for a language specification.
 *
 * To create a new instance of this interface, use the {@link ILanguageSpecConfigBuilder} interface.
 */
public interface ISpoofaxLanguageSpecConfig extends ISpoofaxProjectConfig, ILanguageSpecConfig {

    /**
     * Gets the SDF version to use.
     *
     * @return Sdf version to use.
     */
    SdfVersion sdfVersion();

    /**
     * Gets the (relative) path to the sdf main file.
     *
     * @return path to the parse table.
     */
    String sdfMainFile();
    
    /**
     * Gets the (relative) path to the sdf meta files.
     *
     * @return path to the sdf meta files.
     */
    List<String> sdfMetaFiles();

    /**
     * Gets the Placeholder fences.
     * 
     * @return Placeholder fences
     */
    PlaceholderCharacters placeholderChars();

    /**
     * Gets the language to be pretty printed.
     * 
     * @return Language to be pretty printed
     */
    String prettyPrintLanguage();

    /**
     * Gets the language to be pretty printed.
     * 
     * @return Language to be pretty printed
     */
    boolean generateNamespacedGrammar();

    /**
     * Gets the external def.
     *
     * @return The external def.
     */
    @Nullable String sdfExternalDef();

    /**
     * Gets SDF arguments.
     *
     * @return An iterable of SDF arguments.
     */
    Arguments sdfArgs();


    /**
     * Gets the project artifact build setting.
     *
     * @return A member of the {@link StrategoVersion} enumeration.
     */
    StrategoVersion strVersion();

    /**
     * Gets the project artifact format.
     *
     * @return A member of the {@link StrategoFormat} enumeration.
     */
    StrategoFormat strFormat();

    /**
     * Gets the external JAR.
     *
     * @return The external JAR.
     */
    @Nullable String strExternalJar();

    /**
     * Gets the external JAR flags.
     *
     * @return The external JAR flags.
     */
    @Nullable String strExternalJarFlags();

    /**
     * Gets the Stratego arguments.
     *
     * @return The Stratego arguments.
     */
    Arguments strArgs();

    /**
     * Gets Stratego shadow jar setting
     * 
     * @return The Stratego shadow jar setting.
     */
    boolean strShadowJar();

    /**
     * Gets additional build step configurations.
     * 
     * @return Additional build step configurations.
     */
    Collection<IBuildStepConfig> buildSteps();


    /**
     * Gets the ESV name.
     *
     * @return The ESV name.
     */
    String esvName();

    /**
     * Gets the SDF name.
     *
     * @return The SDF name.
     */
    String sdfName();

    /**
     * Gets the meta SDF name.
     *
     * @return The meta SDF name.
     */
    String metaSdfName();

    /**
     * Gets the Stratego name.
     *
     * @return The Stratego name.
     */
    String strategoName();

    /**
     * Gets the package name.
     *
     * @return The package name.
     */
    String packageName();

    /**
     * Gets the Java name.
     *
     * @return The Java name.
     */
    String javaName();
}
