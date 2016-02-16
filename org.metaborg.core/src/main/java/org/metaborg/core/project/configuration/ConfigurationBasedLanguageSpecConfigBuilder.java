package org.metaborg.core.project.configuration;

import java.util.Set;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.language.LanguageContributionIdentifier;
import org.metaborg.core.language.LanguageIdentifier;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.virtlink.commons.configuration2.jackson.JacksonConfiguration;

/**
 * Configuration-based builder for {@link ILanguageSpecConfig} objects.
 */
public class ConfigurationBasedLanguageSpecConfigBuilder implements ILanguageSpecConfigBuilder {
    private final ConfigurationReaderWriter configurationReaderWriter;

    protected @Nullable LanguageIdentifier identifier = null;
    protected @Nullable String name = null;
    protected final Set<LanguageIdentifier> compileDependencies = Sets.newHashSet();
    protected final Set<LanguageIdentifier> runtimeDependencies = Sets.newHashSet();
    protected final Set<LanguageContributionIdentifier> languageContributions = Sets.newHashSet();
    protected @Nullable String metaborgVersion = null;


    /**
     * Initializes a new instance of the {@link ConfigurationBasedLanguageSpecConfigBuilder} class.
     *
     * @param configurationReaderWriter
     *            The configuration reader/writer.
     */
    @Inject public ConfigurationBasedLanguageSpecConfigBuilder(ConfigurationReaderWriter configurationReaderWriter) {
        this.configurationReaderWriter = configurationReaderWriter;
    }


    @Override public ILanguageSpecConfig build(@Nullable FileObject rootFolder) throws IllegalStateException {
        if(!isValid()) {
            throw new IllegalStateException(validateOrError());
        }

        final JacksonConfiguration configuration = createConfiguration(rootFolder);

        return new ConfigurationBasedLanguageSpecConfig(configuration, identifier, name, compileDependencies,
            runtimeDependencies, languageContributions, metaborgVersion);
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
        if(this.name == null) {
            return "A Name must be specified.";
        }
        if(this.identifier == null) {
            return "An Identifier must be specified.";
        }
        return null;
    }

    @Override public ILanguageSpecConfigBuilder reset() {
        identifier = null;
        name = null;
        compileDependencies.clear();
        runtimeDependencies.clear();
        languageContributions.clear();
        return this;
    }

    @Override public ILanguageSpecConfigBuilder copyFrom(ILanguageSpecConfig config) {
        withIdentifier(config.identifier());
        withName(config.name());
        withCompileDependencies(config.compileDependencies());
        withRuntimeDependencies(config.runtimeDependencies());
        withLanguageContributions(config.languageContributions());
        return this;
    }

    @Override public ILanguageSpecConfigBuilder withIdentifier(LanguageIdentifier identifier) {
        this.identifier = identifier;
        return this;
    }

    @Override public ILanguageSpecConfigBuilder withName(String name) {
        this.name = name;
        return null;
    }

    @Override public ILanguageSpecConfigBuilder withCompileDependencies(Iterable<LanguageIdentifier> dependencies) {
        this.compileDependencies.clear();
        return addCompileDependencies(dependencies);
    }

    @Override public ILanguageSpecConfigBuilder addCompileDependencies(Iterable<LanguageIdentifier> dependencies) {
        this.compileDependencies.addAll(Lists.newArrayList(dependencies));
        return this;
    }

    @Override public ILanguageSpecConfigBuilder withRuntimeDependencies(Iterable<LanguageIdentifier> dependencies) {
        this.runtimeDependencies.clear();
        return addRuntimeDependencies(dependencies);
    }

    @Override public ILanguageSpecConfigBuilder addRuntimeDependencies(Iterable<LanguageIdentifier> dependencies) {
        this.runtimeDependencies.addAll(Lists.newArrayList(dependencies));
        return this;
    }

    @Override public ILanguageSpecConfigBuilder withLanguageContributions(
        Iterable<LanguageContributionIdentifier> contributions) {
        this.languageContributions.clear();
        return addLanguageContributions(contributions);
    }

    @Override public ILanguageSpecConfigBuilder addLanguageContributions(
        Iterable<LanguageContributionIdentifier> contributions) {
        this.languageContributions.addAll(Lists.newArrayList(contributions));
        return this;
    }

    @Override public ILanguageSpecConfigBuilder withMetaborgVersion(String metaborgVersion) {
        this.metaborgVersion = metaborgVersion;
        return this;
    }
}
