package org.metaborg.spoofax.core.processing;

import org.metaborg.core.language.ILanguageService;
import org.metaborg.core.processing.IProcessor;
import org.metaborg.core.processing.ProcessorRunner;
import org.spoofax.interpreter.terms.IStrategoTerm;

import com.google.inject.Inject;

/**
 * Typedef class for {@link ProcessorRunner} with {@link IStrategoTerm}.
 */
public class SpoofaxProcessorRunner extends ProcessorRunner<IStrategoTerm, IStrategoTerm, IStrategoTerm> implements
    ISpoofaxProcessorRunner {
    @Inject public SpoofaxProcessorRunner(IProcessor<IStrategoTerm, IStrategoTerm, IStrategoTerm> processor,
        ILanguageService languageService) {
        super(processor, languageService);
    }
}
