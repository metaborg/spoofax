package org.metaborg.spoofax.core.processing;

import org.metaborg.spoofax.core.syntax.ISyntaxService;
import org.spoofax.interpreter.terms.IStrategoTerm;

import com.google.inject.Inject;

/**
 * Typedef class for {@link ParseResultProcessor} with {@link IStrategoTerm}.
 */
public class SpoofaxParseResultProcessor extends ParseResultProcessor<IStrategoTerm> implements
    ISpoofaxParseResultProcessor {
    @Inject public SpoofaxParseResultProcessor(ISyntaxService<IStrategoTerm> syntaxService) {
        super(syntaxService);
    }
}
