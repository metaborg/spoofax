package org.metaborg.spoofax.core.project.configuration;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import java.nio.file.Path;
import java.nio.file.Paths;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.ImmutableConfiguration;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.metaborg.core.language.LanguageContributionIdentifier;
import org.metaborg.core.language.LanguageIdentifier;
import org.metaborg.core.project.NameUtil;
import org.metaborg.core.project.configuration.ConfigurationBasedLanguageSpecConfig;
import org.metaborg.spoofax.core.project.settings.Format;
import org.metaborg.util.cmd.Arguments;

/**
 * An implementation of the {@link ISpoofaxLanguageSpecConfig} interface
 * that is backed by an {@link ImmutableConfiguration} object.
 */
public class ConfigurationBasedSpoofaxLanguageSpecConfig extends ConfigurationBasedLanguageSpecConfig implements ISpoofaxLanguageSpecConfig {

    private static final long serialVersionUID = -2143964605340506212L;
    /* package private */ static final String PROP_FORMAT = "format";
    /* package private */ static final String PROP_EXTERNAL_DEF = "externalDef";
    /* package private */ static final String PROP_EXTERNAL_JAR = "externalJar.name";
    /* package private */ static final String PROP_EXTERNAL_JAR_FLAGS = "externalJar.flags";
    /* package private */ static final String PROP_SDF_ARGS = "language.sdf.args";
    /* package private */ static final String PROP_STRATEGO_ARGS = "language.stratego.args";
    /* package private */ static final String PROP_PARDONED_LANGUAGES = "pardonedLanguages";

    /**
     * Initializes a new instance of the {@link ConfigurationBasedSpoofaxLanguageSpecConfig} class.
     *
     * @param configuration The configuration that provides the properties.
     */
    public ConfigurationBasedSpoofaxLanguageSpecConfig(final HierarchicalConfiguration<ImmutableNode> configuration) {
        super(configuration);
    }

    /**
     * Initializes a new instance of the {@link ConfigurationBasedSpoofaxLanguageSpecConfig} class.
     *
     * Use the {@link ConfigurationBasedSpoofaxLanguageSpecConfigBuilder} instead.
     *
     * @param configuration The configuration that provides some of the properties.
     */
    protected ConfigurationBasedSpoofaxLanguageSpecConfig(
            final HierarchicalConfiguration<ImmutableNode> configuration,
            final LanguageIdentifier identifier,
            final String name,
            final Collection<LanguageIdentifier> compileDependencies,
            final Collection<LanguageIdentifier> runtimeDependencies,
            final Collection<LanguageContributionIdentifier> languageContributions,
            final Collection<String> pardonedLanguages,
            final Format format,
            final String externalDef,
            final String externalJar,
            final String externalJarFlags,
            final Arguments sdfArgs,
            final Arguments strategoArgs
    ) {
        super(configuration, identifier, name, compileDependencies, runtimeDependencies, languageContributions);
        configuration.setProperty(PROP_PARDONED_LANGUAGES, pardonedLanguages);
        configuration.setProperty(PROP_FORMAT, format);
        configuration.setProperty(PROP_EXTERNAL_DEF, externalDef);
        configuration.setProperty(PROP_EXTERNAL_JAR, externalJar);
        configuration.setProperty(PROP_EXTERNAL_JAR_FLAGS, externalJarFlags);
        configuration.setProperty(PROP_SDF_ARGS, sdfArgs);
        configuration.setProperty(PROP_STRATEGO_ARGS, strategoArgs);
    }

    /**
     * {@inheritDoc}
     *
     * The pardoned languages are stored as a list at the <code>pardonedLanguages</code> element.
     * Each pardoned language is the language name.
     */
    @Override
    public Collection<String> pardonedLanguages() {
        @Nullable final List<String> value = this.config.getList(String.class, PROP_PARDONED_LANGUAGES);
        return value != null ? value : Collections.<String>emptyList();
    }

    /**
     * {@inheritDoc}
     *
     * The format is stored at the <code>format</code> element.
     * The format is either <code>jar</code> or <code>ctree</code>.
     */
    public Format format() {
        @Nullable String value = this.config.getString(PROP_FORMAT);
        return value != null ? Format.valueOf(value) : Format.ctree;
    }

    /**
     * {@inheritDoc}
     *
     * The SDF name is not stored in the configuration.
     */
    @Override
    public String sdfName() { return name(); }

    /**
     * {@inheritDoc}
     *
     * The meta-SDF name is not stored in the configuration.
     */
    @Override
    public String metaSdfName() { return sdfName() + "-Stratego"; }

    /**
     * {@inheritDoc}
     *
     * The SDF arguments are stored as a list at the <code>language.sdf.args</code> element.
     * Each argument is a string.
     */
    public Arguments sdfArgs() {
        @Nullable final List<String> values = this.config.getList(String.class, PROP_SDF_ARGS);
        final Arguments arguments = new Arguments();
        if (values != null) {
            for (String value : values) {
                arguments.add(value);
            }
        }
        return arguments;
    }

    /**
     * {@inheritDoc}
     *
     * The external definition is stored at the <code>externalDef</code> element.
     */
    @Nullable
    public String externalDef() {
        return this.config.getString(PROP_EXTERNAL_DEF);
    }

    /**
     * {@inheritDoc}
     *
     * The Stratego arguments are stored as a list at the <code>language.stratego.args</code> element.
     * Each argument is a string.
     */
    public Arguments strategoArgs() {
        @Nullable final List<String> values = this.config.getList(String.class, PROP_STRATEGO_ARGS);
        final Arguments arguments = new Arguments();
        if (values != null) {
            for (String value : values) {
                arguments.add(value);
            }
        }
        return arguments;
    }

    /**
     * {@inheritDoc}
     *
     * The external JAR name is stored at the <code>externalJar.name</code> element.
     */
    @Nullable
    public String externalJar() {
        return this.config.getString(PROP_EXTERNAL_JAR);
    }

    /**
     * {@inheritDoc}
     *
     * The external JAR flags are stored at the <code>externalJar.flags</code> element.
     */
    @Nullable
    public String externalJarFlags() {
        return this.config.getString(PROP_EXTERNAL_JAR_FLAGS);
    }

    /**
     * {@inheritDoc}
     *
     * The Stratego name is not stored in the configuration.
     */
    public String strategoName() {
        return NameUtil.toJavaId(this.name().toLowerCase());
    }

    /**
     * {@inheritDoc}
     *
     * The Java name is not stored in the configuration.
     */
    public String javaName() {
        return NameUtil.toJavaId(this.name());
    }

    /**
     * {@inheritDoc}
     *
     * The package name is not stored in the configuration.
     */
    public String packageName() {
        return NameUtil.toJavaId(this.identifier().id);
    }

    /**
     * {@inheritDoc}
     *
     * The Strategies package name is not stored in the configuration.
     */
    @Override
    public String strategiesPackageName() { return packageName() + ".strategies"; }

    /**
     * {@inheritDoc}
     *
     * The ESV name is not stored in the configuration.
     */
    @Override
    public String esvName() { return name(); }

}
