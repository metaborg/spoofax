package org.metaborg.core.config;

import java.util.Collection;
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
import org.metaborg.core.messages.IMessage;
import org.metaborg.core.messages.MessageBuilder;

/**
 * Configuration-based builder for {@link ILanguageComponentConfig} objects.
 */
public class LanguageComponentConfigBuilder extends ProjectConfigBuilder implements ILanguageComponentConfigBuilder {
    protected @Nullable LanguageIdentifier identifier = null;
    protected @Nullable String name = null;
    protected @Nullable Set<LanguageContributionIdentifier> langContribs;
    protected @Nullable List<IGenerateConfig> generates;
    protected @Nullable List<IExportConfig> exports;

    @Inject public LanguageComponentConfigBuilder(AConfigurationReaderWriter configReaderWriter) {
        super(configReaderWriter);
    }


    @Override public ILanguageComponentConfig build(@Nullable FileObject rootFolder) throws IllegalStateException {
        if (this.configuration == null)
            this.configuration = this.configReaderWriter.create(null, rootFolder);

        final LanguageComponentConfig config = new LanguageComponentConfig(
                this.configuration, this.metaborgVersion, this.identifier, this.name,
                this.compileDeps, this.sourceDeps, this.javaDeps, this.typesmart,
                this.langContribs, this.generates, this.exports);
        validateOrThrow(config);
        return config;
    }

    private void validateOrThrow(LanguageComponentConfig config) {
        final Collection<IMessage> messages = config.validate(MessageBuilder.create());
        if (messages.isEmpty())
            return;

        throw new IllegalStateException(messages.iterator().next().toString());
    }

    @Override public ILanguageComponentConfigBuilder reset() {
        super.reset();
        this.identifier = null;
        this.name = null;
        this.langContribs = null;
        this.generates = null;
        this.exports = null;

        return this;
    }

    @Override public ILanguageComponentConfigBuilder copyFrom(ILanguageComponentConfig config) {
        super.copyFrom(config);
        if (!(config instanceof IConfig)) {
            withIdentifier(config.identifier());
            withName(config.name());
            withLangContribs(config.langContribs());
            withGenerates(config.generates());
            withExports(config.exports());
        }
        return this;
    }


    @Override public ILanguageComponentConfigBuilder withMetaborgVersion(String metaborgVersion) {
        super.withMetaborgVersion(metaborgVersion);
        return this;
    }

    @Override public ILanguageComponentConfigBuilder withCompileDeps(Iterable<LanguageIdentifier> deps) {
        super.withCompileDeps(deps);
        return this;
    }

    @Override public ILanguageComponentConfigBuilder addCompileDeps(Iterable<LanguageIdentifier> deps) {
        super.addCompileDeps(deps);
        return this;
    }

    @Override public ILanguageComponentConfigBuilder withSourceDeps(Iterable<LanguageIdentifier> deps) {
        super.withSourceDeps(deps);
        return this;
    }

    @Override public ILanguageComponentConfigBuilder addSourceDeps(Iterable<LanguageIdentifier> deps) {
        super.addSourceDeps(deps);
        return this;
    }

    @Override public ILanguageComponentConfigBuilder withJavaDeps(Iterable<LanguageIdentifier> deps) {
        super.withJavaDeps(deps);
        return this;
    }

    @Override public ILanguageComponentConfigBuilder addJavaDeps(Iterable<LanguageIdentifier> deps) {
        super.addJavaDeps(deps);
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

    @Override public ILanguageComponentConfigBuilder
        withLangContribs(Iterable<LanguageContributionIdentifier> contribs) {
        if (this.langContribs != null)
            this.langContribs.clear();

        addLangContribs(contribs);
        return this;
    }

    @Override public ILanguageComponentConfigBuilder
        addLangContribs(Iterable<LanguageContributionIdentifier> contribs) {
        if (this.langContribs == null)
            this.langContribs = Sets.newHashSet();

        Iterables.addAll(this.langContribs, contribs);
        return this;
    }

    @Override public ILanguageComponentConfigBuilder withGenerates(Iterable<IGenerateConfig> generates) {
        if (this.generates != null)
            this.generates.clear();

        addGenerates(generates);
        return this;
    }

    @Override public ILanguageComponentConfigBuilder addGenerates(Iterable<IGenerateConfig> generates) {
        if (this.generates == null)
            this.generates = Lists.newArrayList();

        Iterables.addAll(this.generates, generates);
        return this;
    }

    @Override public ILanguageComponentConfigBuilder withExports(Iterable<IExportConfig> exports) {
        if (this.exports != null)
            this.exports.clear();

        addExports(exports);
        return this;
    }

    @Override public ILanguageComponentConfigBuilder addExports(Iterable<IExportConfig> exports) {
        if (this.exports == null)
            this.exports = Lists.newArrayList();

        Iterables.addAll(this.exports, exports);
        return this;
    }
}
