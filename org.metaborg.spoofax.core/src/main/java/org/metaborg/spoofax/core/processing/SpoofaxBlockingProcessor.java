package org.metaborg.spoofax.core.processing;

import org.metaborg.core.build.IBuilder;
import org.metaborg.core.language.dialect.IDialectProcessor;
import org.metaborg.core.processing.BlockingProcessor;
import org.metaborg.core.processing.ILanguageChangeProcessor;
import org.spoofax.interpreter.terms.IStrategoTerm;

import com.google.inject.Inject;

/**
 * Typedef class for {@link BlockingProcessor} with {@link IStrategoTerm}.
 */
public class SpoofaxBlockingProcessor extends BlockingProcessor<IStrategoTerm, IStrategoTerm, IStrategoTerm> implements
    ISpoofaxProcessor {
    @Inject public SpoofaxBlockingProcessor(IDialectProcessor dialectProcessor,
        IBuilder<IStrategoTerm, IStrategoTerm, IStrategoTerm> builder, ILanguageChangeProcessor languageChangeProcessor) {
        super(dialectProcessor, builder, languageChangeProcessor);
    }
}
