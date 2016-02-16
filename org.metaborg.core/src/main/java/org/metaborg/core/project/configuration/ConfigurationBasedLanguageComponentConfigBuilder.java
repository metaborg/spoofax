package org.metaborg.core.project.configuration;

import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.language.LanguageContributionIdentifier;
import org.metaborg.core.language.LanguageIdentifier;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.virtlink.commons.configuration2.jackson.JacksonConfiguration;

/**
 * Configuration-based builder for {@link ILanguageComponentConfig} objects.
 */
public class ConfigurationBasedLanguageComponentConfigBuilder implements ILanguageComponentConfigBuilder {
    protected final ConfigurationReaderWriter configurationReaderWriter;

    protected @Nullable LanguageIdentifier identifier = null;
    protected @Nullable String name = null;
    protected final Set<LanguageIdentifier> compileDeps = Sets.newHashSet();
    protected final Set<LanguageIdentifier> sourceDeps = Sets.newHashSet();
    protected final Set<LanguageIdentifier> javaDeps = Sets.newHashSet();
    protected final Set<LanguageContributionIdentifier> langContribs = Sets.newHashSet();
    protected final List<Generate> generates = Lists.newArrayList();
    protected final List<Export> exports = Lists.newArrayList();


    /**
     * Initializes a new instance of the {@link ConfigurationBasedLanguageComponentConfigBuilder} class.
     *
     * @param configurationReaderWriter
     *            The configuration reader/writer.
     */
    @Inject public ConfigurationBasedLanguageComponentConfigBuilder(
        ConfigurationReaderWriter configurationReaderWriter) {
        this.configurationReaderWriter = configurationReaderWriter;
    }


    @Override public ILanguageComponentConfig build(@Nullable FileObject rootFolder) throws IllegalStateException {
        if(!isValid()) {
            throw new IllegalStateException(validateOrError());
        }

        final JacksonConfiguration configuration = createConfiguration(rootFolder);

        return new ConfigurationBasedLanguageComponentConfig(configuration, identifier, name, compileDeps, sourceDeps,
            langContribs);
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
        if(name == null) {
            return "A Name must be specified.";
        }
        if(identifier == null) {
            return "An Identifier must be specified.";
        }

        return null;
    }

    @Override public ILanguageComponentConfigBuilder reset() {
        identifier = null;
        name = null;
        compileDeps.clear();
        sourceDeps.clear();
        return this;
    }

    @Override public ILanguageComponentConfigBuilder copyFrom(ILanguageComponentConfig config) {
        withIdentifier(config.identifier());
        withName(config.name());
        withCompileDeps(config.compileDeps());
        withSourceDeps(config.sourceDeps());
        withLangContribs(config.langContribs());
        return this;
    }

    @Override public ILanguageComponentConfigBuilder copyFrom(ILanguageSpecConfig config) {
        withIdentifier(config.identifier());
        withName(config.name());
        withCompileDeps(config.compileDeps());
        withSourceDeps(config.sourceDeps());
        withLangContribs(config.langContribs());
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

    @Override public ILanguageComponentConfigBuilder withCompileDeps(Iterable<LanguageIdentifier> deps) {
        this.compileDeps.clear();
        addCompileDeps(deps);
        return this;
    }

    @Override public ILanguageComponentConfigBuilder addCompileDeps(Iterable<LanguageIdentifier> deps) {
        Iterables.addAll(this.compileDeps, deps);
        return this;
    }

    @Override public ILanguageComponentConfigBuilder withSourceDeps(Iterable<LanguageIdentifier> deps) {
        this.sourceDeps.clear();
        addSourceDeps(deps);
        return this;
    }

    @Override public ILanguageComponentConfigBuilder addSourceDeps(Iterable<LanguageIdentifier> deps) {
        Iterables.addAll(this.sourceDeps, deps);
        return this;
    }

    @Override public ILanguageComponentConfigBuilder withJavaDeps(Iterable<LanguageIdentifier> deps) {
        this.javaDeps.clear();
        addJavaDeps(deps);
        return this;
    }

    @Override public ILanguageComponentConfigBuilder addJavaDeps(Iterable<LanguageIdentifier> deps) {
        Iterables.addAll(this.javaDeps, deps);
        return this;
    }

    @Override public ILanguageComponentConfigBuilder
        withLangContribs(Iterable<LanguageContributionIdentifier> contribs) {
        this.langContribs.clear();
        addLangContribs(contribs);
        return this;
    }

    @Override public ILanguageComponentConfigBuilder
        addLangContribs(Iterable<LanguageContributionIdentifier> contribs) {
        Iterables.addAll(this.langContribs, contribs);
        return this;
    }

    @Override public ILanguageComponentConfigBuilder withGenerates(Iterable<Generate> generates) {
        this.generates.clear();
        addGenerates(generates);
        return this;
    }

    @Override public ILanguageComponentConfigBuilder addGenerates(Iterable<Generate> generates) {
        Iterables.addAll(this.generates, generates);
        return this;
    }

    @Override public ILanguageComponentConfigBuilder withExports(Iterable<Export> exports) {
        this.exports.clear();
        addExports(exports);
        return this;
    }

    @Override public ILanguageComponentConfigBuilder addExports(Iterable<Export> exports) {
        Iterables.addAll(this.exports, exports);
        return this;
    }
}
