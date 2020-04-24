package org.metaborg.spoofax.meta.core;

import org.metaborg.meta.core.MetaborgMetaModule;
import org.metaborg.meta.core.config.ILanguageSpecConfigBuilder;
import org.metaborg.meta.core.config.ILanguageSpecConfigService;
import org.metaborg.meta.core.config.ILanguageSpecConfigWriter;
import org.metaborg.meta.core.config.LanguageSpecConfigService;
import org.metaborg.meta.core.project.ILanguageSpecService;
import org.metaborg.spoofax.meta.core.ant.AntRunnerService;
import org.metaborg.spoofax.meta.core.ant.IAntRunnerService;
import org.metaborg.spoofax.meta.core.build.AntBuildStep;
import org.metaborg.spoofax.meta.core.build.IBuildStep;
import org.metaborg.spoofax.meta.core.build.LanguageSpecBuilder;
import org.metaborg.spoofax.meta.core.build.StrategoBuildStep;
import org.metaborg.spoofax.meta.core.config.ISpoofaxLanguageSpecConfigBuilder;
import org.metaborg.spoofax.meta.core.config.ISpoofaxLanguageSpecConfigService;
import org.metaborg.spoofax.meta.core.config.ISpoofaxLanguageSpecConfigWriter;
import org.metaborg.spoofax.meta.core.config.SpoofaxLanguageSpecConfigBuilder;
import org.metaborg.spoofax.meta.core.config.SpoofaxLanguageSpecConfigService;
import org.metaborg.spoofax.meta.core.pluto.build.main.IPieProvider;
import org.metaborg.spoofax.meta.core.pluto.build.main.PieProvider;
import org.metaborg.spoofax.meta.core.project.ISpoofaxLanguageSpecService;
import org.metaborg.spoofax.meta.core.project.SpoofaxLanguageSpecService;
import org.metaborg.spoofax.meta.core.stratego.primitive.CheckSdf2TablePrimitive;
import org.metaborg.spoofax.meta.core.stratego.primitive.LanguageSpecPpNamePrimitive;
import org.metaborg.spoofax.meta.core.stratego.primitive.LanguageSpecSrcGenDirectory;
import org.metaborg.spoofax.meta.core.stratego.primitive.LanguageSpecificationPrimitive;
import org.metaborg.spoofax.meta.core.stratego.primitive.LegacyLanguageSpecNamePrimitive;
import org.metaborg.spoofax.meta.core.stratego.primitive.PlaceholderCharsPrimitive;
import org.metaborg.spoofax.meta.core.stratego.primitive.StrategoPieAnalyzePrimitive;

import com.google.inject.Singleton;
import com.google.inject.multibindings.Multibinder;

public class SpoofaxMetaModule extends MetaborgMetaModule {
    @Override protected void configure() {
        super.configure();

        bind(LanguageSpecBuilder.class).in(Singleton.class);
        autoClosableBinder.addBinding().to(LanguageSpecBuilder.class);

        final Multibinder<IBuildStep> buildStepBinder = Multibinder.newSetBinder(binder(), IBuildStep.class);
        buildStepBinder.addBinding().to(AntBuildStep.class);
        buildStepBinder.addBinding().to(StrategoBuildStep.class);

        bindAnt();
        bindPie();

        // Static injections for SpoofaxExtensionModule bindings.
        requestStaticInjection(LanguageSpecificationPrimitive.class);
        requestStaticInjection(LanguageSpecSrcGenDirectory.class);
        requestStaticInjection(LanguageSpecPpNamePrimitive.class);
        requestStaticInjection(CheckSdf2TablePrimitive.class);
        requestStaticInjection(PlaceholderCharsPrimitive.class);
        requestStaticInjection(StrategoPieAnalyzePrimitive.class);

        requestStaticInjection(LegacyLanguageSpecNamePrimitive.class);
    }

    private void bindPie() {
        bind(IPieProvider.class).to(PieProvider.class).in(Singleton.class);
    }

    protected void bindAnt() {
        bind(IAntRunnerService.class).to(AntRunnerService.class).in(Singleton.class);
    }

    /**
     * Overrides {@link MetaborgMetaModule#bindLanguageSpec()} for Spoofax implementation of language specifications.
     */
    @Override protected void bindLanguageSpec() {
        bind(SpoofaxLanguageSpecService.class).in(Singleton.class);
        bind(ILanguageSpecService.class).to(SpoofaxLanguageSpecService.class);
        bind(ISpoofaxLanguageSpecService.class).to(SpoofaxLanguageSpecService.class);
    }

    /**
     * Overrides {@link MetaborgMetaModule#bindLanguageSpecConfig()} for Spoofax implementation of language
     * specification configuration.
     */
    @Override protected void bindLanguageSpecConfig() {
        bind(ILanguageSpecConfigWriter.class).to(LanguageSpecConfigService.class).in(Singleton.class);

        bind(SpoofaxLanguageSpecConfigService.class).in(Singleton.class);
        bind(ILanguageSpecConfigService.class).to(SpoofaxLanguageSpecConfigService.class);
        bind(ISpoofaxLanguageSpecConfigService.class).to(SpoofaxLanguageSpecConfigService.class);
        bind(ISpoofaxLanguageSpecConfigWriter.class).to(SpoofaxLanguageSpecConfigService.class);

        bind(SpoofaxLanguageSpecConfigBuilder.class);
        bind(ILanguageSpecConfigBuilder.class).to(SpoofaxLanguageSpecConfigBuilder.class);
        bind(ISpoofaxLanguageSpecConfigBuilder.class).to(SpoofaxLanguageSpecConfigBuilder.class);
    }
}
