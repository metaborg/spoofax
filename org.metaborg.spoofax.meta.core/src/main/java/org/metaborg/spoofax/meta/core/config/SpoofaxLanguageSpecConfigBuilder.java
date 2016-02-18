package org.metaborg.spoofax.meta.core.config;

import java.util.Set;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.config.AConfigurationReaderWriter;
import org.metaborg.core.config.IExport;
import org.metaborg.core.config.IGenerate;
import org.metaborg.core.language.LanguageContributionIdentifier;
import org.metaborg.core.language.LanguageIdentifier;
import org.metaborg.meta.core.config.ILanguageSpecConfig;
import org.metaborg.meta.core.config.LanguageSpecConfigBuilder;
import org.metaborg.spoofax.core.project.settings.StrategoFormat;
import org.metaborg.util.cmd.Arguments;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.virtlink.commons.configuration2.jackson.JacksonConfiguration;

/**
 * Configuration-based builder for {@link ILanguageSpecConfig} objects.
 */
public class SpoofaxLanguageSpecConfigBuilder extends LanguageSpecConfigBuilder
    implements ISpoofaxLanguageSpecConfigBuilder {
    protected final Set<String> pardonedLanguages = Sets.newHashSet();
    protected @Nullable StrategoFormat format = null;
    protected @Nullable String externalDef = null;
    protected @Nullable String externalJar = null;
    protected @Nullable String externalJarFlags = null;
    protected final Arguments sdfArgs = new Arguments();
    protected final Arguments strategoArgs = new Arguments();


    @Inject public SpoofaxLanguageSpecConfigBuilder(final AConfigurationReaderWriter configReaderWriter) {
        super(configReaderWriter);
    }


    @Override public ISpoofaxLanguageSpecConfig build(@Nullable FileObject rootFolder) throws IllegalStateException {
        if(!isValid()) {
            throw new IllegalStateException(validateOrError());
        }

        final JacksonConfiguration configuration = configReaderWriter.createConfiguration(null, rootFolder);
        return new SpoofaxLanguageSpecConfig(configuration, identifier, name, compileDeps, sourceDeps, javaDeps,
            langContribs, generates, exports, metaborgVersion, pardonedLanguages, format, externalDef, externalJar,
            externalJarFlags, sdfArgs, strategoArgs);
    }

    @Override public ISpoofaxLanguageSpecConfigBuilder reset() {
        super.reset();
        this.pardonedLanguages.clear();
        this.format = null;
        this.sdfArgs.clear();
        this.strategoArgs.clear();
        this.externalDef = null;
        this.externalJar = null;
        this.externalJarFlags = null;
        return this;
    }

    @Override public ISpoofaxLanguageSpecConfigBuilder copyFrom(ISpoofaxLanguageSpecConfig config) {
        super.copyFrom(config);
        withPardonedLanguages(config.pardonedLanguages());
        withFormat(config.format());
        withExternalDef(config.externalDef());
        withExternalJar(config.externalJar());
        withExternalJarFlags(config.externalJarFlags());
        withSdfArgs(config.sdfArgs());
        withStrategoArgs(config.strategoArgs());
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

    @Override public ISpoofaxLanguageSpecConfigBuilder withGenerates(Iterable<IGenerate> generates) {
        super.withGenerates(generates);
        return this;
    }

    @Override public ISpoofaxLanguageSpecConfigBuilder addGenerates(Iterable<IGenerate> generates) {
        super.addGenerates(generates);
        return this;
    }

    @Override public ISpoofaxLanguageSpecConfigBuilder withExports(Iterable<IExport> exports) {
        super.withExports(exports);
        return this;
    }

    @Override public ISpoofaxLanguageSpecConfigBuilder addExports(Iterable<IExport> exports) {
        super.addExports(exports);
        return this;
    }

    @Override public ISpoofaxLanguageSpecConfigBuilder withPardonedLanguages(Iterable<String> languages) {
        this.pardonedLanguages.clear();
        return addPardonedLanguages(languages);
    }

    @Override public ISpoofaxLanguageSpecConfigBuilder addPardonedLanguages(Iterable<String> languages) {
        this.pardonedLanguages.addAll(Lists.newArrayList(languages));
        return this;
    }

    @Override public ISpoofaxLanguageSpecConfigBuilder withFormat(StrategoFormat format) {
        this.format = format;
        return this;
    }

    @Override public ISpoofaxLanguageSpecConfigBuilder withExternalDef(String def) {
        this.externalDef = def;
        return this;
    }

    @Override public ISpoofaxLanguageSpecConfigBuilder withExternalJar(String jar) {
        this.externalJar = jar;
        return this;
    }

    @Override public ISpoofaxLanguageSpecConfigBuilder withExternalJarFlags(String flags) {
        this.externalJarFlags = flags;
        return this;
    }

    @Override public ISpoofaxLanguageSpecConfigBuilder withSdfArgs(Arguments args) {
        this.sdfArgs.addAll(new Arguments(args));
        return this;
    }

    @Override public ISpoofaxLanguageSpecConfigBuilder withStrategoArgs(Arguments args) {
        this.strategoArgs.addAll(new Arguments(args));
        return this;
    }
}
