package org.metaborg.meta.core;

import org.metaborg.meta.core.config.ILanguageSpecConfigBuilder;
import org.metaborg.meta.core.config.ILanguageSpecConfigService;
import org.metaborg.meta.core.config.ILanguageSpecConfigWriter;
import org.metaborg.meta.core.config.LanguageSpecConfigBuilder;
import org.metaborg.meta.core.config.LanguageSpecConfigService;
import org.metaborg.meta.core.signature.ISignatureExtractor;
import org.metaborg.meta.core.signature.ISignatureService;
import org.metaborg.meta.core.signature.SignatureService;

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
        final Multibinder<ISignatureExtractor> signatureExtractors =
            Multibinder.newSetBinder(binder(), ISignatureExtractor.class);
        bindSignatureExtractors(signatureExtractors);
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
        bind(ISignatureService.class).to(SignatureService.class).in(Singleton.class);
    }

    /**
     * @param signatureExtractors
     *            Signature extractors multibinder.
     */
    protected void bindSignatureExtractors(Multibinder<ISignatureExtractor> signatureExtractors) {

    }
}
