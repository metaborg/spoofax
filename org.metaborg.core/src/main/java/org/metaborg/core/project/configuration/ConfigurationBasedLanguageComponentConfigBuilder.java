package org.metaborg.core.project.configuration;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nullable;

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

    /**
     * Initializes a new instance of the {@link ConfigurationBasedLanguageComponentConfigBuilder} class.
     *
     * @param configurationReaderWriter The configuration reader/writer.
     */
    @Inject
    public ConfigurationBasedLanguageComponentConfigBuilder(final ConfigurationReaderWriter configurationReaderWriter) {
        this.configurationReaderWriter = configurationReaderWriter;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ILanguageComponentConfig build() throws IllegalStateException {
        if (!isValid())
            throw new IllegalStateException(validateOrError());

        JacksonConfiguration configuration = createConfiguration();

        return new ConfigurationBasedLanguageComponentConfig(
                configuration,
                this.identifier,
                this.name,
                this.compileDependencies,
                this.runtimeDependencies);
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
    public ILanguageComponentConfigBuilder reset() {
        this.identifier = null;
        this.name = null;
        this.compileDependencies.clear();
        this.runtimeDependencies.clear();

        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ILanguageComponentConfigBuilder copyFrom(ILanguageComponentConfig config) {
        withIdentifier(config.identifier());
        withName(config.name());
        withCompileDependencies(config.compileDependencies());
        withRuntimeDependencies(config.runtimeDependencies());

        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ILanguageComponentConfigBuilder withIdentifier(LanguageIdentifier identifier) {
        this.identifier = identifier;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ILanguageComponentConfigBuilder withName(String name) {
        this.name = name;
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ILanguageComponentConfigBuilder withCompileDependencies(Iterable<LanguageIdentifier> dependencies) {
        this.compileDependencies.clear();
        return addCompileDependencies(dependencies);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ILanguageComponentConfigBuilder addCompileDependencies(Iterable<LanguageIdentifier> dependencies) {
        this.compileDependencies.addAll(Lists.newArrayList(dependencies));
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ILanguageComponentConfigBuilder withRuntimeDependencies(Iterable<LanguageIdentifier> dependencies) {
        this.runtimeDependencies.clear();
        return addRuntimeDependencies(dependencies);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ILanguageComponentConfigBuilder addRuntimeDependencies(Iterable<LanguageIdentifier> dependencies) {
        this.runtimeDependencies.addAll(Lists.newArrayList(dependencies));
        return this;
    }

}
