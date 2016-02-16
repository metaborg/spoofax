package org.metaborg.core.project.configuration;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.MetaborgConstants;
import org.metaborg.core.language.LanguageContributionIdentifier;
import org.metaborg.core.language.LanguageIdentifier;

import com.google.inject.Inject;
import com.virtlink.commons.configuration2.jackson.JacksonConfiguration;

/**
 * Configuration-based builder for {@link ILanguageSpecConfig} objects.
 */
public class ConfigurationBasedLanguageSpecConfigBuilder extends ConfigurationBasedLanguageComponentConfigBuilder
    implements ILanguageSpecConfigBuilder {
    protected String metaborgVersion = MetaborgConstants.METABORG_VERSION;


    /**
     * Initializes a new instance of the {@link ConfigurationBasedLanguageSpecConfigBuilder} class.
     *
     * @param configurationReaderWriter
     *            The configuration reader/writer.
     */
    @Inject public ConfigurationBasedLanguageSpecConfigBuilder(ConfigurationReaderWriter configurationReaderWriter) {
        super(configurationReaderWriter);
    }


    @Override public ILanguageSpecConfig build(@Nullable FileObject rootFolder) throws IllegalStateException {
        if(!isValid()) {
            throw new IllegalStateException(validateOrError());
        }

        final JacksonConfiguration configuration = createConfiguration(rootFolder);

        return new ConfigurationBasedLanguageSpecConfig(configuration, identifier, name, compileDeps, sourceDeps,
            langContribs, metaborgVersion);
    }

    /**
     * Validates the builder; or returns an error message.
     *
     * @return <code>null</code> when the builder is valid; otherwise, an error message when the builder is invalid.
     */
    protected String validateOrError() {
        return super.validateOrError();
    }

    @Override public ILanguageSpecConfigBuilder reset() {
        super.reset();
        metaborgVersion = null;
        return this;
    }

    @Override public ILanguageSpecConfigBuilder copyFrom(ILanguageSpecConfig config) {
        withIdentifier(config.identifier());
        withName(config.name());
        withCompileDeps(config.compileDeps());
        withSourceDeps(config.sourceDeps());
        withLangContribs(config.langContribs());
        withMetaborgVersion(config.metaborgVersion());
        return this;
    }

    @Override public ILanguageSpecConfigBuilder withIdentifier(LanguageIdentifier identifier) {
        super.withIdentifier(identifier);
        return this;
    }

    @Override public ILanguageSpecConfigBuilder withName(String name) {
        super.withName(name);
        return this;
    }

    @Override public ILanguageSpecConfigBuilder withCompileDeps(Iterable<LanguageIdentifier> dependencies) {
        super.withCompileDeps(dependencies);
        return this;
    }

    @Override public ILanguageSpecConfigBuilder addCompileDeps(Iterable<LanguageIdentifier> dependencies) {
        super.addCompileDeps(dependencies);
        return this;
    }

    @Override public ILanguageSpecConfigBuilder withSourceDeps(Iterable<LanguageIdentifier> dependencies) {
        super.withSourceDeps(dependencies);
        return this;
    }

    @Override public ILanguageSpecConfigBuilder addSourceDeps(Iterable<LanguageIdentifier> dependencies) {
        super.addSourceDeps(dependencies);
        return this;
    }

    @Override public ILanguageSpecConfigBuilder withJavaDeps(Iterable<LanguageIdentifier> deps) {
        super.withJavaDeps(deps);
        return this;
    }

    @Override public ILanguageSpecConfigBuilder addJavaDeps(Iterable<LanguageIdentifier> deps) {
        super.addJavaDeps(deps);
        return this;
    }

    @Override public ILanguageSpecConfigBuilder withLangContribs(Iterable<LanguageContributionIdentifier> contribs) {
        super.withLangContribs(contribs);
        return this;
    }

    @Override public ILanguageSpecConfigBuilder addLangContribs(Iterable<LanguageContributionIdentifier> contribs) {
        super.addLangContribs(contribs);
        return this;
    }

    @Override public ILanguageSpecConfigBuilder withGenerates(Iterable<Generate> generates) {
        super.withGenerates(generates);
        return this;
    }

    @Override public ILanguageSpecConfigBuilder addGenerates(Iterable<Generate> generates) {
        super.addGenerates(generates);
        return this;
    }

    @Override public ILanguageSpecConfigBuilder withExports(Iterable<Export> exports) {
        super.withExports(exports);
        return this;
    }

    @Override public ILanguageSpecConfigBuilder addExports(Iterable<Export> exports) {
        super.addExports(exports);
        return this;
    }

    @Override public ILanguageSpecConfigBuilder withMetaborgVersion(String metaborgVersion) {
        this.metaborgVersion = metaborgVersion;
        return this;
    }
}
