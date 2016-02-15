package org.metaborg.core.project.configuration;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.language.LanguageContributionIdentifier;
import org.metaborg.core.language.LanguageIdentifier;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.virtlink.commons.configuration2.jackson.JacksonConfiguration;

/**
 * Configuration-based builder for {@link ILanguageComponentConfig} objects.
 */
public class ConfigurationBasedLanguageComponentConfigBuilder implements ILanguageComponentConfigBuilder {
    private final ConfigurationReaderWriter configurationReaderWriter;

    @Nullable protected LanguageIdentifier identifier = null;
    @Nullable protected String name = null;
    protected final Set<LanguageIdentifier> compileDependencies = new HashSet<>();
    protected final Set<LanguageIdentifier> runtimeDependencies = new HashSet<>();
    protected final Set<LanguageContributionIdentifier> languageContributions = new HashSet<>();
    @Nullable protected String metaborgVersion = null;


    /**
     * Initializes a new instance of the {@link ConfigurationBasedLanguageComponentConfigBuilder} class.
     *
     * @param configurationReaderWriter
     *            The configuration reader/writer.
     */
    @Inject public ConfigurationBasedLanguageComponentConfigBuilder(
        final ConfigurationReaderWriter configurationReaderWriter) {
        this.configurationReaderWriter = configurationReaderWriter;
    }


    @Override public ILanguageComponentConfig build(@Nullable FileObject rootFolder) throws IllegalStateException {
        if(!isValid())
            throw new IllegalStateException(validateOrError());

        JacksonConfiguration configuration = createConfiguration(rootFolder);

        return new ConfigurationBasedLanguageComponentConfig(configuration, this.identifier, this.name,
            this.compileDependencies, this.runtimeDependencies, this.languageContributions, this.metaborgVersion);
    }

    /**
     * Builds the configuration.
     *
     * @param rootFolder
     *            The root folder.
     * @return The built configuration.
     */
    protected JacksonConfiguration createConfiguration(@Nullable FileObject rootFolder) {
        return this.configurationReaderWriter.createConfiguration(null, rootFolder);
    }

    @Override public boolean isValid() {
        return validateOrError() == null;
    }

    /**
     * Validates the builder; or returns an error message.
     *
     * @return <code>null</code> when the builder is valid; otherwise, an error message when the builder is invalid.
     */
    protected String validateOrError() {
        if(this.name == null)
            return "A Name must be specified.";
        if(this.identifier == null)
            return "An Identifier must be specified.";

        return null;
    }

    @Override public ILanguageComponentConfigBuilder reset() {
        this.identifier = null;
        this.name = null;
        this.compileDependencies.clear();
        this.runtimeDependencies.clear();

        return this;
    }

    @Override public ILanguageComponentConfigBuilder copyFrom(ILanguageComponentConfig config) {
        withIdentifier(config.identifier());
        withName(config.name());
        withCompileDependencies(config.compileDependencies());
        withRuntimeDependencies(config.runtimeDependencies());
        withLanguageContributions(config.languageContributions());

        return this;
    }

    @Override public ILanguageComponentConfigBuilder copyFrom(ILanguageSpecConfig config) {
        withIdentifier(config.identifier());
        withName(config.name());
        withCompileDependencies(config.compileDependencies());
        withRuntimeDependencies(config.runtimeDependencies());
        withLanguageContributions(config.languageContributions());

        return this;
    }

    @Override public ILanguageComponentConfigBuilder withIdentifier(LanguageIdentifier identifier) {
        this.identifier = identifier;
        return this;
    }

    @Override public ILanguageComponentConfigBuilder withName(String name) {
        this.name = name;
        return this;
    }

    @Override public ILanguageComponentConfigBuilder withCompileDependencies(Iterable<LanguageIdentifier> dependencies) {
        this.compileDependencies.clear();
        return addCompileDependencies(dependencies);
    }

    @Override public ILanguageComponentConfigBuilder addCompileDependencies(Iterable<LanguageIdentifier> dependencies) {
        this.compileDependencies.addAll(Lists.newArrayList(dependencies));
        return this;
    }

    @Override public ILanguageComponentConfigBuilder withRuntimeDependencies(Iterable<LanguageIdentifier> dependencies) {
        this.runtimeDependencies.clear();
        return addRuntimeDependencies(dependencies);
    }

    @Override public ILanguageComponentConfigBuilder addRuntimeDependencies(Iterable<LanguageIdentifier> dependencies) {
        this.runtimeDependencies.addAll(Lists.newArrayList(dependencies));
        return this;
    }

    @Override public ILanguageComponentConfigBuilder withLanguageContributions(
        Iterable<LanguageContributionIdentifier> contributions) {
        this.languageContributions.clear();
        return addLanguageContributions(contributions);
    }

    @Override public ILanguageComponentConfigBuilder addLanguageContributions(
        Iterable<LanguageContributionIdentifier> contributions) {
        this.languageContributions.addAll(Lists.newArrayList(contributions));
        return this;
    }

    @Override public ILanguageComponentConfigBuilder withMetaborgVersion(String metaborgVersion) {
        this.metaborgVersion = metaborgVersion;
        return this;
    }
}
