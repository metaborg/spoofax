package org.metaborg.spoofax.core.project.configuration;

import javax.annotation.Nullable;

import org.metaborg.core.project.configuration.ILanguageSpecConfig;
import org.metaborg.core.project.configuration.ILanguageSpecConfigBuilder;
import org.metaborg.spoofax.core.project.settings.Format;

import java.util.Collection;

/**
 * Spoofax-specific configuration for a language specification.
 *
 * To create a new instance of this interface, use the {@link ILanguageSpecConfigBuilder} interface.
 */
public interface ISpoofaxLanguageSpecConfig extends ILanguageSpecConfig {

    /**
     * Gets a sequence of languages whose errors are ignored.
     *
     * @return The pardoned languages.
     */
    Collection<String> pardonedLanguages();

    /**
     * Gets the project artifact format.
     *
     * @return A member of the {@link Format} enumeration.
     */
    Format format();

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
     * Gets SDF arguments.
     *
     * @return An iterable of SDF arguments.
     */
    Iterable<String> sdfArgs();

    /**
     * Gets the Stratego arguments.
     *
     * @return The Stratego arguments.
     */
    Iterable<String> strategoArgs();

    /**
     * Gets the external def.
     *
     * @return The external def.
     */
    @Nullable
    String externalDef();

    /**
     * Gets the external JAR.
     *
     * @return The external JAR.
     */
    @Nullable
    String externalJar();

    /**
     * Gets the external JAR flags.
     *
     * @return The external JAR flags.
     */
    @Nullable
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
     * Gets the strategies package name.
     *
     * @return The strategies package name.
     */
    String strategiesPackageName();

    /**
     * Gets the ESV name.
     *
     * @return The ESV name.
     */
    String esvName();

}
