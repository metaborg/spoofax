package org.metaborg.spoofax.core.project.configuration;

import java.util.Set;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.language.LanguageContributionIdentifier;
import org.metaborg.core.language.LanguageIdentifier;
import org.metaborg.core.project.configuration.ConfigurationBasedLanguageSpecConfigBuilder;
import org.metaborg.core.project.configuration.ConfigurationReaderWriter;
import org.metaborg.core.project.configuration.ILanguageSpecConfig;
import org.metaborg.spoofax.core.project.settings.Format;
import org.metaborg.util.cmd.Arguments;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.virtlink.commons.configuration2.jackson.JacksonConfiguration;

/**
 * Configuration-based builder for {@link ILanguageSpecConfig} objects.
 */
public class ConfigurationBasedSpoofaxLanguageSpecConfigBuilder extends ConfigurationBasedLanguageSpecConfigBuilder
    implements ISpoofaxLanguageSpecConfigBuilder {
    protected final Set<String> pardonedLanguages = Sets.newHashSet();
    protected @Nullable Format format = null;
    protected @Nullable String externalDef = null;
    protected @Nullable String externalJar = null;
    protected @Nullable String externalJarFlags = null;
    protected final Arguments sdfArgs = new Arguments();
    protected final Arguments strategoArgs = new Arguments();


    /**
     * Initializes a new instance of the {@link ConfigurationBasedSpoofaxLanguageSpecConfigBuilder} class.
     *
     * @param configurationReaderWriter
     *            The configuration reader/writer.
     */
    @Inject public ConfigurationBasedSpoofaxLanguageSpecConfigBuilder(
        final ConfigurationReaderWriter configurationReaderWriter) {
        super(configurationReaderWriter);
    }


    @Override public ISpoofaxLanguageSpecConfig build(@Nullable FileObject rootFolder) throws IllegalStateException {
        if(!isValid()) {
            throw new IllegalStateException(validateOrError());
        }

        final JacksonConfiguration configuration = createConfiguration(rootFolder);

        return new ConfigurationBasedSpoofaxLanguageSpecConfig(configuration, identifier, name, compileDependencies,
            runtimeDependencies, languageContributions, metaborgVersion, pardonedLanguages, format, externalDef,
            externalJar, externalJarFlags, sdfArgs, strategoArgs);
    }

    @Override protected String validateOrError() {
        return null;
    }

    @Override public ISpoofaxLanguageSpecConfigBuilder reset() {
        super.reset();
        this.pardonedLanguages.clear();
        this.format = null;
        this.sdfArgs.clear();
        this.strategoArgs.clear();
        this.externalDef = null;
        this.externalJar = null;
        this.externalJarFlags = null;
        return this;
    }

    @Override public ISpoofaxLanguageSpecConfigBuilder copyFrom(ISpoofaxLanguageSpecConfig config) {
        super.copyFrom(config);
        withPardonedLanguages(config.pardonedLanguages());
        withFormat(config.format());
        withExternalDef(config.externalDef());
        withExternalJar(config.externalJar());
        withExternalJarFlags(config.externalJarFlags());
        withSdfArgs(config.sdfArgs());
        withStrategoArgs(config.strategoArgs());
        return this;
    }

    @Override public ISpoofaxLanguageSpecConfigBuilder withIdentifier(LanguageIdentifier identifier) {
        super.withIdentifier(identifier);
        return this;
    }

    @Override public ISpoofaxLanguageSpecConfigBuilder withName(String name) {
        super.withName(name);
        return this;
    }

    @Override public ISpoofaxLanguageSpecConfigBuilder
        withCompileDependencies(Iterable<LanguageIdentifier> dependencies) {
        super.withCompileDependencies(dependencies);
        return this;
    }

    @Override public ISpoofaxLanguageSpecConfigBuilder
        addCompileDependencies(Iterable<LanguageIdentifier> dependencies) {
        super.addCompileDependencies(dependencies);
        return this;
    }

    @Override public ISpoofaxLanguageSpecConfigBuilder
        withRuntimeDependencies(Iterable<LanguageIdentifier> dependencies) {
        super.withRuntimeDependencies(dependencies);
        return this;
    }

    @Override public ISpoofaxLanguageSpecConfigBuilder
        addRuntimeDependencies(Iterable<LanguageIdentifier> dependencies) {
        super.addRuntimeDependencies(dependencies);
        return this;
    }

    @Override public ISpoofaxLanguageSpecConfigBuilder withLanguageContributions(
        Iterable<LanguageContributionIdentifier> contributions) {
        super.withLanguageContributions(contributions);
        return this;
    }

    @Override public ISpoofaxLanguageSpecConfigBuilder addLanguageContributions(
        Iterable<LanguageContributionIdentifier> contributions) {
        super.addLanguageContributions(contributions);
        return this;
    }

    @Override public ISpoofaxLanguageSpecConfigBuilder withPardonedLanguages(Iterable<String> languages) {
        this.pardonedLanguages.clear();
        return addPardonedLanguages(languages);
    }

    @Override public ISpoofaxLanguageSpecConfigBuilder addPardonedLanguages(Iterable<String> languages) {
        this.pardonedLanguages.addAll(Lists.newArrayList(languages));
        return this;
    }

    @Override public ISpoofaxLanguageSpecConfigBuilder withFormat(Format format) {
        this.format = format;
        return this;
    }

    @Override public ISpoofaxLanguageSpecConfigBuilder withExternalDef(String def) {
        this.externalDef = def;
        return this;
    }

    @Override public ISpoofaxLanguageSpecConfigBuilder withExternalJar(String jar) {
        this.externalJar = jar;
        return this;
    }

    @Override public ISpoofaxLanguageSpecConfigBuilder withExternalJarFlags(String flags) {
        this.externalJarFlags = flags;
        return this;
    }

    @Override public ISpoofaxLanguageSpecConfigBuilder withSdfArgs(Arguments args) {
        this.sdfArgs.addAll(new Arguments(args));
        return this;
    }

    @Override public ISpoofaxLanguageSpecConfigBuilder withStrategoArgs(Arguments args) {
        this.strategoArgs.addAll(new Arguments(args));
        return this;
    }
}
