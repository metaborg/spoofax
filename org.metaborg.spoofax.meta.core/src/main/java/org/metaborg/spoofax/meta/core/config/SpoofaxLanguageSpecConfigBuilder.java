package org.metaborg.spoofax.meta.core.config;

import java.util.Collection;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.config.AConfigurationReaderWriter;
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
import com.virtlink.commons.configuration2.jackson.JacksonConfiguration;

/**
 * Configuration-based builder for {@link ILanguageSpecConfig} objects.
 */
public class SpoofaxLanguageSpecConfigBuilder extends LanguageSpecConfigBuilder
    implements ISpoofaxLanguageSpecConfigBuilder {
    protected SdfVersion sdfVersion = SdfVersion.sdf3;
    protected @Nullable String sdfExternalDef = null;
    protected Arguments sdfArgs = new Arguments();
    protected StrategoFormat strFormat = StrategoFormat.ctree;
    protected @Nullable String strExternalJar = null;
    protected @Nullable String strExternalJarFlags = null;
    protected Arguments strArgs = new Arguments();
    protected Collection<IBuildStepConfig> buildSteps = Lists.newArrayList();


    @Inject public SpoofaxLanguageSpecConfigBuilder(final AConfigurationReaderWriter configReaderWriter) {
        super(configReaderWriter);
    }


    @Override public ISpoofaxLanguageSpecConfig build(@Nullable FileObject rootFolder) throws IllegalStateException {
        if(!isValid()) {
            throw new IllegalStateException(validateOrError());
        }

        final JacksonConfiguration configuration = configReaderWriter.create(null, rootFolder);
        return new SpoofaxLanguageSpecConfig(configuration, identifier, name, compileDeps, sourceDeps, javaDeps,
            langContribs, generates, exports, metaborgVersion, pardonedLanguages, useBuildSystemSpec, sdfVersion,
            sdfExternalDef, sdfArgs, strFormat, strExternalJar, strExternalJarFlags, strArgs, buildSteps);
    }

    @Override public ISpoofaxLanguageSpecConfigBuilder reset() {
        super.reset();
        this.sdfVersion = SdfVersion.sdf3;
        this.sdfExternalDef = null;
        this.sdfArgs.clear();
        this.strFormat = StrategoFormat.ctree;
        this.strExternalJar = null;
        this.strExternalJarFlags = null;
        this.strArgs.clear();
        this.buildSteps.clear();
        return this;
    }

    @Override public ISpoofaxLanguageSpecConfigBuilder copyFrom(ISpoofaxLanguageSpecConfig config) {
        super.copyFrom(config);
        withSdfVersion(config.sdfVersion());
        withSdfExternalDef(config.sdfExternalDef());
        withSdfArgs(config.sdfArgs());
        withStrFormat(config.strFormat());
        withStrExternalJar(config.strExternalJar());
        withStrExternalJarFlags(config.strExternalJarFlags());
        withStrArgs(config.strArgs());
        withBuildSteps(config.buildSteps());
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
        return null;
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

    @Override public ISpoofaxLanguageSpecConfigBuilder withStrArgs(Arguments args) {
        this.strArgs = args;
        return this;
    }


    @Override public ISpoofaxLanguageSpecConfigBuilder withBuildSteps(Iterable<IBuildStepConfig> buildSteps) {
        this.buildSteps.clear();
        addBuildSteps(buildSteps);
        return this;
    }

    @Override public ISpoofaxLanguageSpecConfigBuilder addBuildSteps(Iterable<IBuildStepConfig> buildSteps) {
        Iterables.addAll(this.buildSteps, buildSteps);
        return this;
    }
}
