package org.metaborg.core.config;

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
public class LanguageComponentConfigBuilder extends ProjectConfigBuilder implements ILanguageComponentConfigBuilder {
    protected @Nullable LanguageIdentifier identifier = null;
    protected @Nullable String name = null;
    protected final Set<LanguageContributionIdentifier> langContribs = Sets.newHashSet();
    protected final List<IGenerateConfig> generates = Lists.newArrayList();
    protected final List<IExportConfig> exports = Lists.newArrayList();
    protected boolean typesmart = false;

    @Inject public LanguageComponentConfigBuilder(AConfigurationReaderWriter configReaderWriter) {
        super(configReaderWriter);
    }


    @Override public ILanguageComponentConfig build(@Nullable FileObject rootFolder) throws IllegalStateException {
        if(!isValid()) {
            throw new IllegalStateException(validateOrError());
        }

        final JacksonConfiguration configuration = configReaderWriter.create(null, rootFolder);
        return new LanguageComponentConfig(configuration, metaborgVersion, identifier, name, compileDeps, sourceDeps,
            javaDeps, langContribs, generates, exports, typesmart);
    }

    @Override public boolean isValid() {
        return validateOrError() == null;
    }

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
        super.reset();
        identifier = null;
        name = null;
        langContribs.clear();
        generates.clear();
        exports.clear();
        typesmart = false;
        return this;
    }

    @Override public ILanguageComponentConfigBuilder copyFrom(ILanguageComponentConfig config) {
        super.copyFrom(config);
        withIdentifier(config.identifier());
        withName(config.name());
        withLangContribs(config.langContribs());
        withGenerates(config.generates());
        withExports(config.exports());
        withTypesmart(config.typesmart());
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
        this.langContribs.clear();
        addLangContribs(contribs);
        return this;
    }

    @Override public ILanguageComponentConfigBuilder
        addLangContribs(Iterable<LanguageContributionIdentifier> contribs) {
        Iterables.addAll(this.langContribs, contribs);
        return this;
    }

    @Override public ILanguageComponentConfigBuilder withGenerates(Iterable<IGenerateConfig> generates) {
        this.generates.clear();
        addGenerates(generates);
        return this;
    }

    @Override public ILanguageComponentConfigBuilder addGenerates(Iterable<IGenerateConfig> generates) {
        Iterables.addAll(this.generates, generates);
        return this;
    }

    @Override public ILanguageComponentConfigBuilder withExports(Iterable<IExportConfig> exports) {
        this.exports.clear();
        addExports(exports);
        return this;
    }

    @Override public ILanguageComponentConfigBuilder withTypesmart(boolean typesmart) {
        this.typesmart = typesmart;
        return this;
    }

    @Override public ILanguageComponentConfigBuilder addExports(Iterable<IExportConfig> exports) {
        Iterables.addAll(this.exports, exports);
        return this;
    }
}
