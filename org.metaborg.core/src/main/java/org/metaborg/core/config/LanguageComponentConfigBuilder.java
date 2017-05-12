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

/**
 * Configuration-based builder for {@link ILanguageComponentConfig} objects.
 */
public class LanguageComponentConfigBuilder extends AConfigBuilder implements ILanguageComponentConfigBuilder {
    protected final ProjectConfigBuilder projectConfigBuilder;

    protected @Nullable LanguageIdentifier identifier;
    protected @Nullable String name;
    protected @Nullable Set<LanguageContributionIdentifier> langContribs;
    protected @Nullable List<IGenerateConfig> generates;
    protected @Nullable List<IExportConfig> exports;

    protected @Nullable Boolean sdfEnabled;
    protected @Nullable String parseTable;
    protected @Nullable String completionsParseTable;

    @Inject public LanguageComponentConfigBuilder(AConfigurationReaderWriter configReaderWriter) {
        super(configReaderWriter);
        this.projectConfigBuilder = new ProjectConfigBuilder(configReaderWriter);
    }


    @Override public ILanguageComponentConfig build(@Nullable FileObject rootFolder) throws IllegalStateException {
        if(configuration == null) {
            configuration = configReaderWriter.create(null, rootFolder);
        }
        ProjectConfig projectConfig = projectConfigBuilder.build(configuration);
        final LanguageComponentConfig config = new LanguageComponentConfig(configuration, projectConfig, identifier,
                name, sdfEnabled, parseTable, completionsParseTable, langContribs, generates, exports);
        return config;
    }


    @Override public ILanguageComponentConfigBuilder reset() {
        projectConfigBuilder.reset();
        identifier = null;
        name = null;
        langContribs = null;
        generates = null;
        exports = null;
        parseTable = null;
        completionsParseTable = null;
        sdfEnabled = null;
        return this;
    }

    @Override public ILanguageComponentConfigBuilder copyFrom(ILanguageComponentConfig config) {
        if(config instanceof IConfig) {
            this.configuration = cloneConfiguration((IConfig) config);
            projectConfigBuilder.setConfiguration(this.configuration);
        } else {
            withIdentifier(config.identifier());
            withName(config.name());
            withLangContribs(config.langContribs());
            withGenerates(config.generates());
            withExports(config.exports());
            withSdfTable(config.parseTable());
            withSdfCompletionsTable(config.completionsParseTable());
            withSdfEnabled(config.sdfEnabled());
            projectConfigBuilder.copyValuesFrom(config);
        }
        return this;
    }


    @Override public ILanguageComponentConfigBuilder withMetaborgVersion(String metaborgVersion) {
        projectConfigBuilder.withMetaborgVersion(metaborgVersion);
        return this;
    }

    @Override public ILanguageComponentConfigBuilder withCompileDeps(Iterable<LanguageIdentifier> deps) {
        projectConfigBuilder.withCompileDeps(deps);
        return this;
    }

    @Override public ILanguageComponentConfigBuilder addCompileDeps(Iterable<LanguageIdentifier> deps) {
        projectConfigBuilder.addCompileDeps(deps);
        return this;
    }

    @Override public ILanguageComponentConfigBuilder withSourceDeps(Iterable<LanguageIdentifier> deps) {
        projectConfigBuilder.withSourceDeps(deps);
        return this;
    }

    @Override public ILanguageComponentConfigBuilder addSourceDeps(Iterable<LanguageIdentifier> deps) {
        projectConfigBuilder.addSourceDeps(deps);
        return this;
    }

    @Override public ILanguageComponentConfigBuilder withJavaDeps(Iterable<LanguageIdentifier> deps) {
        projectConfigBuilder.withJavaDeps(deps);
        return this;
    }

    @Override public ILanguageComponentConfigBuilder addJavaDeps(Iterable<LanguageIdentifier> deps) {
        projectConfigBuilder.addJavaDeps(deps);
        return this;
    }


    @Override public ILanguageComponentConfigBuilder withIdentifier(LanguageIdentifier identifier) {
        this.identifier = identifier;
        return this;
    }

    @Override public ILanguageComponentConfigBuilder withSdfEnabled(Boolean sdfEnabled) {
        this.sdfEnabled = sdfEnabled;
        return this;
    }

    @Override public ILanguageComponentConfigBuilder withName(String name) {
        this.name = name;
        return this;
    }

    @Override public ILanguageComponentConfigBuilder withSdfTable(String table) {
        this.parseTable = table;
        return this;
    }

    @Override public ILanguageComponentConfigBuilder withSdfCompletionsTable(String completionsTable) {
        this.completionsParseTable = completionsTable;
        return this;
    }

    @Override public ILanguageComponentConfigBuilder
            withLangContribs(Iterable<LanguageContributionIdentifier> contribs) {
        if(this.langContribs != null) {
            this.langContribs.clear();
        }

        addLangContribs(contribs);
        return this;
    }

    @Override public ILanguageComponentConfigBuilder
            addLangContribs(Iterable<LanguageContributionIdentifier> contribs) {
        if(this.langContribs == null) {
            this.langContribs = Sets.newHashSet();
        }

        Iterables.addAll(this.langContribs, contribs);
        return this;
    }

    @Override public ILanguageComponentConfigBuilder withGenerates(Iterable<IGenerateConfig> generates) {
        if(this.generates != null) {
            this.generates.clear();
        }

        addGenerates(generates);
        return this;
    }

    @Override public ILanguageComponentConfigBuilder addGenerates(Iterable<IGenerateConfig> generates) {
        if(this.generates == null) {
            this.generates = Lists.newArrayList();
        }

        Iterables.addAll(this.generates, generates);
        return this;
    }

    @Override public ILanguageComponentConfigBuilder withExports(Iterable<IExportConfig> exports) {
        if(this.exports != null) {
            this.exports.clear();
        }

        addExports(exports);
        return this;
    }

    @Override public ILanguageComponentConfigBuilder addExports(Iterable<IExportConfig> exports) {
        if(this.exports == null) {
            this.exports = Lists.newArrayList();
        }

        Iterables.addAll(this.exports, exports);
        return this;
    }

}
