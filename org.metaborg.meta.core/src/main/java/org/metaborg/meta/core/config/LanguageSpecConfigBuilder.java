package org.metaborg.meta.core.config;

import java.util.Set;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.config.AConfigurationReaderWriter;
import org.metaborg.core.config.IConfig;
import org.metaborg.core.config.IExportConfig;
import org.metaborg.core.config.IGenerateConfig;
import org.metaborg.core.config.LanguageComponentConfigBuilder;
import org.metaborg.core.language.LanguageContributionIdentifier;
import org.metaborg.core.language.LanguageIdentifier;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.inject.Inject;

/**
 * Configuration-based builder for {@link ILanguageSpecConfig} objects.
 */
public class LanguageSpecConfigBuilder extends LanguageComponentConfigBuilder implements ILanguageSpecConfigBuilder {
    protected @Nullable Set<String> pardonedLanguages;
    protected @Nullable Boolean useBuildSystemSpec;


    @Inject public LanguageSpecConfigBuilder(AConfigurationReaderWriter configReaderWriter) {
        super(configReaderWriter);
    }


    @Override public ILanguageSpecConfig build(@Nullable FileObject rootFolder) throws IllegalStateException {
        if(configuration == null) {
            configuration = configReaderWriter.create(null, rootFolder);
        }
        final LanguageSpecConfig config = new LanguageSpecConfig(configuration, metaborgVersion, identifier, name,
            compileDeps, sourceDeps, javaDeps, sdfEnabled, parseTable, completionsParseTable, typesmart, langContribs,
            generates, exports, pardonedLanguages, useBuildSystemSpec);
        return config;
    }

    @Override public ILanguageSpecConfigBuilder reset() {
        super.reset();
        pardonedLanguages = null;
        useBuildSystemSpec = null;
        return this;
    }

    @Override public ILanguageSpecConfigBuilder copyFrom(ILanguageSpecConfig config) {
        super.copyFrom(config);
        if(!(config instanceof IConfig)) {
            withPardonedLanguages(config.pardonedLanguages());
            withUseBuildSystemSpec(config.useBuildSystemSpec());
        }
        return this;
    }


    @Override public ILanguageSpecConfigBuilder withMetaborgVersion(String metaborgVersion) {
        super.withMetaborgVersion(metaborgVersion);
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

    @Override public ILanguageSpecConfigBuilder withGenerates(Iterable<IGenerateConfig> generates) {
        super.withGenerates(generates);
        return this;
    }

    @Override public ILanguageSpecConfigBuilder addGenerates(Iterable<IGenerateConfig> generates) {
        super.addGenerates(generates);
        return this;
    }

    @Override public ILanguageSpecConfigBuilder withExports(Iterable<IExportConfig> exports) {
        super.withExports(exports);
        return this;
    }

    @Override public ILanguageSpecConfigBuilder addExports(Iterable<IExportConfig> exports) {
        super.addExports(exports);
        return this;
    }


    @Override public ILanguageSpecConfigBuilder withPardonedLanguages(Iterable<String> languages) {
        if(this.pardonedLanguages != null) {
            this.pardonedLanguages.clear();
        }

        return addPardonedLanguages(languages);
    }

    @Override public ILanguageSpecConfigBuilder addPardonedLanguages(Iterable<String> languages) {
        if(this.pardonedLanguages == null) {
            this.pardonedLanguages = Sets.newHashSet();
        }

        this.pardonedLanguages.addAll(Lists.newArrayList(languages));
        return this;
    }

    @Override public ILanguageSpecConfigBuilder withUseBuildSystemSpec(boolean useBuildSystemSpec) {
        this.useBuildSystemSpec = useBuildSystemSpec;
        return this;
    }
}
