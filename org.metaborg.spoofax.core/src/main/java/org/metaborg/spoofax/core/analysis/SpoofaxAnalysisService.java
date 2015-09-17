package org.metaborg.spoofax.core.analysis;

import java.util.Map;

import org.metaborg.core.analysis.AnalysisService;
import org.metaborg.core.analysis.IAnalyzer;
import org.spoofax.interpreter.terms.IStrategoTerm;

import com.google.inject.Inject;

/**
 * Typedef class for {@link AnalysisService} with {@link IStrategoTerm}.
 */
public class SpoofaxAnalysisService extends AnalysisService<IStrategoTerm, IStrategoTerm> implements
    ISpoofaxAnalysisService {
    @Inject public SpoofaxAnalysisService(Map<String, IAnalyzer<IStrategoTerm, IStrategoTerm>> analyzers) {
        super(analyzers);
    }
}
