package org.metaborg.core.project.configuration;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.virtlink.commons.configuration2.jackson.JacksonConfiguration;
import org.metaborg.core.language.LanguageContributionIdentifier;
import org.metaborg.core.language.LanguageIdentifier;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;

/**
 * Configuration-based builder for {@link ILanguageSpecConfig} objects.
 */
public class ConfigurationBasedLanguageSpecConfigBuilder implements ILanguageSpecConfigBuilder {

    private final ConfigurationReaderWriter configurationReaderWriter;

    @Nullable protected LanguageIdentifier identifier = null;
    @Nullable protected String name = null;
    protected final Set<LanguageIdentifier> compileDependencies = new HashSet<>();
    protected final Set<LanguageIdentifier> runtimeDependencies = new HashSet<>();
    protected final Set<LanguageContributionIdentifier> languageContributions = new HashSet<>();
    protected final Set<String> pardonedLanguages = new HashSet<>();

    /**
     * Initializes a new instance of the {@link ConfigurationBasedLanguageSpecConfigBuilder} class.
     *
     * @param configurationReaderWriter The configuration reader/writer.
     */
    @Inject
    public ConfigurationBasedLanguageSpecConfigBuilder(final ConfigurationReaderWriter configurationReaderWriter) {
        this.configurationReaderWriter = configurationReaderWriter;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ILanguageSpecConfig build() throws IllegalStateException {
        if (!isValid())
            throw new IllegalStateException(validateOrError());

        JacksonConfiguration configuration = createConfiguration();

        return new ConfigurationBasedLanguageSpecConfig(
                configuration,
                this.identifier,
                this.name,
                this.compileDependencies,
                this.runtimeDependencies,
                this.languageContributions,
                this.pardonedLanguages);
    }

    /**
     * Builds the configuration.
     *
     * @return The built configuration.
     */
    protected JacksonConfiguration createConfiguration() {
        return this.configurationReaderWriter.createConfiguration(null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isValid() {
        return validateOrError() == null;
    }

    /**
     * Validates the builder; or returns an error message.
     *
     * @return <code>null</code> when the builder is valid;
     * otherwise, an error message when the builder is invalid.
     */
    protected String validateOrError() {
        if (this.name == null)
            return "A Name must be specified.";
        if (this.identifier == null)
            return "An Identifier must be specified.";

        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ILanguageSpecConfigBuilder reset() {
        this.identifier = null;
        this.name = null;
        this.compileDependencies.clear();
        this.runtimeDependencies.clear();
        this.languageContributions.clear();
        this.pardonedLanguages.clear();

        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ILanguageSpecConfigBuilder copyFrom(ILanguageSpecConfig config) {
        withIdentifier(config.identifier());
        withName(config.name());
        withCompileDependencies(config.compileDependencies());
        withRuntimeDependencies(config.runtimeDependencies());
        withLanguageContributions(config.languageContributions());
        withPardonedLanguages(config.pardonedLanguages());

        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ILanguageSpecConfigBuilder withIdentifier(LanguageIdentifier identifier) {
        this.identifier = identifier;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ILanguageSpecConfigBuilder withName(String name) {
        this.name = name;
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ILanguageSpecConfigBuilder withCompileDependencies(Iterable<LanguageIdentifier> dependencies) {
        this.compileDependencies.clear();
        return addCompileDependencies(dependencies);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ILanguageSpecConfigBuilder addCompileDependencies(Iterable<LanguageIdentifier> dependencies) {
        this.compileDependencies.addAll(Lists.newArrayList(dependencies));
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ILanguageSpecConfigBuilder withRuntimeDependencies(Iterable<LanguageIdentifier> dependencies) {
        this.runtimeDependencies.clear();
        return addRuntimeDependencies(dependencies);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ILanguageSpecConfigBuilder addRuntimeDependencies(Iterable<LanguageIdentifier> dependencies) {
        this.runtimeDependencies.addAll(Lists.newArrayList(dependencies));
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ILanguageSpecConfigBuilder withLanguageContributions(Iterable<LanguageContributionIdentifier> contributions) {
        this.languageContributions.clear();
        return addLanguageContributions(contributions);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ILanguageSpecConfigBuilder addLanguageContributions(Iterable<LanguageContributionIdentifier> contributions) {
        this.languageContributions.addAll(Lists.newArrayList(contributions));
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ILanguageSpecConfigBuilder withPardonedLanguages(Iterable<String> languages) {
        this.pardonedLanguages.clear();
        return addPardonedLanguages(languages);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ILanguageSpecConfigBuilder addPardonedLanguages(Iterable<String> languages) {
        this.pardonedLanguages.addAll(Lists.newArrayList(languages));
        return this;
    }
}
