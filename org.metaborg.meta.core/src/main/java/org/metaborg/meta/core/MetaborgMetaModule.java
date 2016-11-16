package org.metaborg.meta.core;

import org.metaborg.meta.core.config.ILanguageSpecConfigBuilder;
import org.metaborg.meta.core.config.ILanguageSpecConfigService;
import org.metaborg.meta.core.config.ILanguageSpecConfigWriter;
import org.metaborg.meta.core.config.LanguageSpecConfigBuilder;
import org.metaborg.meta.core.config.LanguageSpecConfigService;
import org.metaborg.meta.core.signature.ISigExtractor;
import org.metaborg.meta.core.signature.ISigSerializer;
import org.metaborg.meta.core.signature.ISigService;
import org.metaborg.meta.core.signature.SigService;
import org.metaborg.meta.core.signature.generate.IRawSigGen;
import org.metaborg.meta.core.signature.generate.ISigGen;
import org.metaborg.meta.core.signature.generate.ISigGenService;
import org.metaborg.meta.core.signature.generate.JavaSigGen;
import org.metaborg.meta.core.signature.generate.SigGenService;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.google.inject.multibindings.Multibinder;

public class MetaborgMetaModule extends AbstractModule {
    protected Multibinder<AutoCloseable> autoClosableBinder;


    @Override protected void configure() {
        autoClosableBinder = Multibinder.newSetBinder(binder(), AutoCloseable.class, Meta.class);

        bindLanguageSpec();
        bindLanguageSpecConfig();
        bindSignature();
        final Multibinder<ISigExtractor> sigExtractors = Multibinder.newSetBinder(binder(), ISigExtractor.class);
        bindSigExtractors(sigExtractors);
        final Multibinder<IRawSigGen> rawSigGens = Multibinder.newSetBinder(binder(), IRawSigGen.class);
        final Multibinder<ISigGen> sigGens = Multibinder.newSetBinder(binder(), ISigGen.class);
        bindSigGens(rawSigGens, sigGens);
    }


    protected void bindLanguageSpec() {
    }

    protected void bindLanguageSpecConfig() {
        bind(LanguageSpecConfigService.class).in(Singleton.class);
        bind(ILanguageSpecConfigWriter.class).to(LanguageSpecConfigService.class);
        bind(ILanguageSpecConfigService.class).to(LanguageSpecConfigService.class);

        bind(LanguageSpecConfigBuilder.class);
        bind(ILanguageSpecConfigBuilder.class).to(LanguageSpecConfigBuilder.class);
    }

    protected void bindSignature() {
        bind(SigService.class).in(Singleton.class);
        bind(ISigService.class).to(SigService.class);
        bind(ISigSerializer.class).to(SigService.class);

        bind(SigGenService.class).in(Singleton.class);
        bind(ISigGenService.class).to(SigGenService.class);
    }

    /**
     * @param signatureExtractors
     *            Signature extractors multibinder.
     */
    protected void bindSigExtractors(Multibinder<ISigExtractor> signatureExtractors) {

    }

    /**
     * @param rawSigGens
     *            Raw signature generator multibinder.
     * @param sigGens
     *            Signature generator multibinder.
     */
    protected void bindSigGens(Multibinder<IRawSigGen> rawSigGens, Multibinder<ISigGen> sigGens) {
        sigGens.addBinding().to(JavaSigGen.class).in(Singleton.class);
    }
}
