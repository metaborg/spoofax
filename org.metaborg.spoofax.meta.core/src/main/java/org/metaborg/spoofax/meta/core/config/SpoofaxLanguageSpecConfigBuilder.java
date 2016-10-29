package org.metaborg.spoofax.meta.core.config;

import java.util.Collection;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.config.AConfigurationReaderWriter;
import org.metaborg.core.config.IConfig;
import org.metaborg.core.config.IExportConfig;
import org.metaborg.core.config.IGenerateConfig;
import org.metaborg.core.language.LanguageContributionIdentifier;
import org.metaborg.core.language.LanguageIdentifier;
import org.metaborg.meta.core.config.ILanguageSpecConfig;
import org.metaborg.meta.core.config.LanguageSpecConfigBuilder;
import org.metaborg.util.cmd.Arguments;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.inject.Inject;

/**
 * Configuration-based builder for {@link ILanguageSpecConfig} objects.
 */
public class SpoofaxLanguageSpecConfigBuilder extends LanguageSpecConfigBuilder
    implements ISpoofaxLanguageSpecConfigBuilder {

    protected @Nullable SdfVersion sdfVersion;
    protected @Nullable Sdf2tableVersion sdf2tableVersion;
    protected @Nullable String sdfMainFile;
    protected @Nullable PlaceholderCharacters placeholderCharacters;
    protected @Nullable String prettyPrint;
    protected @Nullable String sdfExternalDef;
    protected @Nullable Arguments sdfArgs;
    protected @Nullable StrategoFormat strFormat;
    protected @Nullable String strExternalJar;
    protected @Nullable String strExternalJarFlags;
    protected @Nullable Arguments strArgs;
    protected @Nullable Collection<IBuildStepConfig> buildSteps;


    @Inject public SpoofaxLanguageSpecConfigBuilder(final AConfigurationReaderWriter configReaderWriter) {
        super(configReaderWriter);
    }


    @Override public ISpoofaxLanguageSpecConfig build(@Nullable FileObject rootFolder) throws IllegalStateException {
        if(configuration == null) {
            configuration = configReaderWriter.create(null, rootFolder);
        }

        final SpoofaxLanguageSpecConfig config = new SpoofaxLanguageSpecConfig(configuration, identifier, name,
            compileDeps, sourceDeps, javaDeps, typesmart, langContribs, generates, exports, metaborgVersion,
            pardonedLanguages, useBuildSystemSpec, sdfVersion, sdfEnabled, sdfMainFile, parseTable,
            completionsParseTable, sdf2tableVersion, placeholderCharacters, prettyPrint, sdfExternalDef, sdfArgs,
            strFormat, strExternalJar, strExternalJarFlags, strArgs, buildSteps);
        return config;

    }

    @Override public ISpoofaxLanguageSpecConfigBuilder reset() {
        super.reset();

        sdfVersion = null;
        sdf2tableVersion = null;
        sdfMainFile = null;
        placeholderCharacters = null;
        prettyPrint = null;
        sdfExternalDef = null;
        sdfArgs = null;
        strFormat = null;
        strExternalJar = null;
        strExternalJarFlags = null;
        strArgs = null;
        buildSteps = null;
        return this;
    }

    @Override public ISpoofaxLanguageSpecConfigBuilder copyFrom(ISpoofaxLanguageSpecConfig config) {
        super.copyFrom(config);

        if(!(config instanceof IConfig)) {
            withSdfVersion(config.sdfVersion());
            withSdf2tableVersion(config.sdf2tableVersion());
            withPrettyPrintLanguage(config.prettyPrintLanguage());
            withSdfMainFile(config.sdfMainFile());
            withPlaceholderPrefix(config.placeholderChars().prefix);
            withPlaceholderPostfix(config.placeholderChars().suffix);
            withSdfExternalDef(config.sdfExternalDef());
            withSdfArgs(config.sdfArgs());
            withStrFormat(config.strFormat());
            withStrExternalJar(config.strExternalJar());
            withStrExternalJarFlags(config.strExternalJarFlags());
            withStrArgs(config.strArgs());
            withBuildSteps(config.buildSteps());
        }
        return this;
    }

    @Override public ISpoofaxLanguageSpecConfigBuilder withPrettyPrintLanguage(String prettyPrintLanguage) {
        this.prettyPrint = prettyPrintLanguage;
        return this;
    }


    @Override public ISpoofaxLanguageSpecConfigBuilder withMetaborgVersion(String metaborgVersion) {
        super.withMetaborgVersion(metaborgVersion);
        return this;
    }

    @Override public ISpoofaxLanguageSpecConfigBuilder withIdentifier(LanguageIdentifier identifier) {
        super.withIdentifier(identifier);
        return this;
    }

    @Override public ISpoofaxLanguageSpecConfigBuilder withName(String name) {
        super.withName(name);
        return this;
    }

    @Override public ISpoofaxLanguageSpecConfigBuilder withCompileDeps(Iterable<LanguageIdentifier> deps) {
        super.withCompileDeps(deps);
        return this;
    }

    @Override public ISpoofaxLanguageSpecConfigBuilder addCompileDeps(Iterable<LanguageIdentifier> deps) {
        super.addCompileDeps(deps);
        return this;
    }

    @Override public ISpoofaxLanguageSpecConfigBuilder withSourceDeps(Iterable<LanguageIdentifier> deps) {
        super.withSourceDeps(deps);
        return this;
    }

    @Override public ISpoofaxLanguageSpecConfigBuilder addSourceDeps(Iterable<LanguageIdentifier> deps) {
        super.addSourceDeps(deps);
        return this;
    }

    @Override public ISpoofaxLanguageSpecConfigBuilder withJavaDeps(Iterable<LanguageIdentifier> deps) {
        super.withJavaDeps(deps);
        return this;
    }

    @Override public ISpoofaxLanguageSpecConfigBuilder addJavaDeps(Iterable<LanguageIdentifier> deps) {
        super.addJavaDeps(deps);
        return this;
    }

    @Override public ISpoofaxLanguageSpecConfigBuilder
        withLangContribs(Iterable<LanguageContributionIdentifier> contribs) {
        super.withLangContribs(contribs);
        return this;
    }

    @Override public ISpoofaxLanguageSpecConfigBuilder
        addLangContribs(Iterable<LanguageContributionIdentifier> contribs) {
        super.addLangContribs(contribs);
        return this;
    }

    @Override public ISpoofaxLanguageSpecConfigBuilder withGenerates(Iterable<IGenerateConfig> generates) {
        super.withGenerates(generates);
        return this;
    }

    @Override public ISpoofaxLanguageSpecConfigBuilder addGenerates(Iterable<IGenerateConfig> generates) {
        super.addGenerates(generates);
        return this;
    }

    @Override public ISpoofaxLanguageSpecConfigBuilder withExports(Iterable<IExportConfig> exports) {
        super.withExports(exports);
        return this;
    }

    @Override public ISpoofaxLanguageSpecConfigBuilder addExports(Iterable<IExportConfig> exports) {
        super.addExports(exports);
        return this;
    }

    @Override public ISpoofaxLanguageSpecConfigBuilder withPardonedLanguages(Iterable<String> languages) {
        super.withPardonedLanguages(languages);
        return this;
    }

    @Override public ISpoofaxLanguageSpecConfigBuilder addPardonedLanguages(Iterable<String> languages) {
        super.addPardonedLanguages(languages);
        return this;
    }

    @Override public ISpoofaxLanguageSpecConfigBuilder withUseBuildSystemSpec(boolean useBuildSystemSpec) {
        super.withUseBuildSystemSpec(useBuildSystemSpec);
        return this;
    }


    @Override public ISpoofaxLanguageSpecConfigBuilder withSdfVersion(SdfVersion sdfVersion) {
        this.sdfVersion = sdfVersion;
        return this;
    }

    @Override public ISpoofaxLanguageSpecConfigBuilder withSdfMainFile(String sdfMainFile) {
        this.sdfMainFile = sdfMainFile;
        return this;
    }

    @Override public ISpoofaxLanguageSpecConfigBuilder withSdf2tableVersion(Sdf2tableVersion sdf2tableVersion) {
        this.sdf2tableVersion = sdf2tableVersion;
        return this;
    }

    @Override public ISpoofaxLanguageSpecConfigBuilder withSdfExternalDef(String def) {
        this.sdfExternalDef = def;
        return this;
    }

    @Override public ISpoofaxLanguageSpecConfigBuilder withSdfArgs(Arguments args) {
        this.sdfArgs = args;
        return this;
    }


    @Override public ISpoofaxLanguageSpecConfigBuilder withStrFormat(StrategoFormat format) {
        this.strFormat = format;
        return this;
    }

    @Override public ISpoofaxLanguageSpecConfigBuilder withStrExternalJar(String jar) {
        this.strExternalJar = jar;
        return this;
    }

    @Override public ISpoofaxLanguageSpecConfigBuilder withStrExternalJarFlags(String flags) {
        this.strExternalJarFlags = flags;
        return this;
    }

    @Override public ISpoofaxLanguageSpecConfigBuilder withStrTypesmart(boolean typesmart) {
        this.typesmart = typesmart;
        return this;
    }

    @Override public ISpoofaxLanguageSpecConfigBuilder withStrArgs(Arguments args) {
        this.strArgs = args;
        return this;
    }


    @Override public ISpoofaxLanguageSpecConfigBuilder withBuildSteps(Iterable<IBuildStepConfig> buildSteps) {
        if(this.buildSteps != null) {
            this.buildSteps.clear();
        }

        addBuildSteps(buildSteps);
        return this;
    }

    @Override public ISpoofaxLanguageSpecConfigBuilder addBuildSteps(Iterable<IBuildStepConfig> buildSteps) {
        if(this.buildSteps == null) {
            buildSteps = Lists.newArrayList();
        }

        Iterables.addAll(this.buildSteps, buildSteps);
        return this;
    }


    @Override public ISpoofaxLanguageSpecConfigBuilder withPlaceholderPrefix(String placeholderPrefix) {
        this.placeholderCharacters.prefix = placeholderPrefix;
        return this;
    }


    @Override public ISpoofaxLanguageSpecConfigBuilder withPlaceholderPostfix(String placeholderPostfix) {
        this.placeholderCharacters.suffix = placeholderPostfix;
        return this;
    }

}
