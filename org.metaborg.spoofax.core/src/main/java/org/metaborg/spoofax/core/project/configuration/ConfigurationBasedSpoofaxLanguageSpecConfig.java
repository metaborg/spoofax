package org.metaborg.spoofax.core.project.configuration;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

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
 * An implementation of the {@link ISpoofaxLanguageSpecConfig} interface that is backed by an
 * {@link ImmutableConfiguration} object.
 */
public class ConfigurationBasedSpoofaxLanguageSpecConfig extends ConfigurationBasedLanguageSpecConfig implements
    ISpoofaxLanguageSpecConfig {
    private static final long serialVersionUID = -2143964605340506212L;

    private static final String PROP_FORMAT = "format";
    private static final String PROP_EXTERNAL_DEF = "externalDef";
    private static final String PROP_EXTERNAL_JAR = "externalJar.name";
    private static final String PROP_EXTERNAL_JAR_FLAGS = "externalJar.flags";
    private static final String PROP_SDF_ARGS = "language.sdf.args";
    private static final String PROP_STRATEGO_ARGS = "language.stratego.args";
    private static final String PROP_PARDONED_LANGUAGES = "pardonedLanguages";


    /**
     * Initializes a new instance of the {@link ConfigurationBasedSpoofaxLanguageSpecConfig} class.
     *
     * @param configuration
     *            The configuration that provides the properties.
     */
    public ConfigurationBasedSpoofaxLanguageSpecConfig(HierarchicalConfiguration<ImmutableNode> configuration) {
        super(configuration);
    }

    /**
     * Initializes a new instance of the {@link ConfigurationBasedSpoofaxLanguageSpecConfig} class.
     *
     * Use the {@link ConfigurationBasedSpoofaxLanguageSpecConfigBuilder} instead.
     *
     * @param configuration
     *            The configuration that provides some of the properties.
     */
    protected ConfigurationBasedSpoofaxLanguageSpecConfig(HierarchicalConfiguration<ImmutableNode> configuration,
        LanguageIdentifier identifier, String name, Collection<LanguageIdentifier> compileDependencies,
        Collection<LanguageIdentifier> runtimeDependencies,
        Collection<LanguageContributionIdentifier> languageContributions, String metaborgVersion,
        Collection<String> pardonedLanguages, Format format, String externalDef, String externalJar,
        String externalJarFlags, Arguments sdfArgs, Arguments strategoArgs) {
        super(configuration, identifier, name, compileDependencies, runtimeDependencies, languageContributions,
            metaborgVersion);

        configuration.setProperty(PROP_PARDONED_LANGUAGES, pardonedLanguages);
        configuration.setProperty(PROP_FORMAT, format);
        configuration.setProperty(PROP_EXTERNAL_DEF, externalDef);
        configuration.setProperty(PROP_EXTERNAL_JAR, externalJar);
        configuration.setProperty(PROP_EXTERNAL_JAR_FLAGS, externalJarFlags);
        configuration.setProperty(PROP_SDF_ARGS, sdfArgs);
        configuration.setProperty(PROP_STRATEGO_ARGS, strategoArgs);
    }


    @Override public Collection<String> pardonedLanguages() {
        final List<String> value = this.config.getList(String.class, PROP_PARDONED_LANGUAGES);
        return value != null ? value : Collections.<String>emptyList();
    }

    public Format format() {
        final String value = this.config.getString(PROP_FORMAT);
        return value != null ? Format.valueOf(value) : Format.ctree;
    }

    @Override public String sdfName() {
        return name();
    }

    @Override public String metaSdfName() {
        return sdfName() + "-Stratego";
    }

    public Arguments sdfArgs() {
        final List<String> values = this.config.getList(String.class, PROP_SDF_ARGS);
        final Arguments arguments = new Arguments();
        if(values != null) {
            for(String value : values) {
                arguments.add(value);
            }
        }
        return arguments;
    }

    @Nullable public String externalDef() {
        return this.config.getString(PROP_EXTERNAL_DEF);
    }

    @Override public Arguments strategoArgs() {
        @Nullable final List<String> values = this.config.getList(String.class, PROP_STRATEGO_ARGS);
        final Arguments arguments = new Arguments();
        if(values != null) {
            for(String value : values) {
                arguments.add(value);
            }
        }
        return arguments;
    }

    @Override public @Nullable String externalJar() {
        return this.config.getString(PROP_EXTERNAL_JAR);
    }

    @Override public @Nullable String externalJarFlags() {
        return this.config.getString(PROP_EXTERNAL_JAR_FLAGS);
    }

    @Override public String strategoName() {
        return NameUtil.toJavaId(this.name().toLowerCase());
    }

    @Override public String javaName() {
        return NameUtil.toJavaId(this.name());
    }

    @Override public String packageName() {
        return NameUtil.toJavaId(this.identifier().id);
    }

    @Override public String strategiesPackageName() {
        return packageName() + ".strategies";
    }

    @Override public String esvName() {
        return name();
    }
}
