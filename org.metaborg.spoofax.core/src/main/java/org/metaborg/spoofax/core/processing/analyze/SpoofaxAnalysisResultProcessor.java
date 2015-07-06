package org.metaborg.spoofax.core.processing.analyze;

import org.metaborg.core.analysis.IAnalysisService;
import org.metaborg.core.processing.analyze.AnalysisResultProcessor;
import org.metaborg.core.processing.parse.IParseResultRequester;
import org.spoofax.interpreter.terms.IStrategoTerm;

import com.google.inject.Inject;

/**
 * Typedef class for {@link AnalysisResultProcessor} with {@link IStrategoTerm}.
 */
public class SpoofaxAnalysisResultProcessor extends AnalysisResultProcessor<IStrategoTerm, IStrategoTerm> implements
    ISpoofaxAnalysisResultProcessor {
    @Inject public SpoofaxAnalysisResultProcessor(IAnalysisService<IStrategoTerm, IStrategoTerm> analysisService,
        IParseResultRequester<IStrategoTerm> parseResultProcessor) {
        super(analysisService, parseResultProcessor);
    }
}
