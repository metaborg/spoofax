package org.metaborg.spoofax.core.analysis;

import org.metaborg.core.analysis.AnalysisService;
import org.spoofax.interpreter.terms.IStrategoTerm;

/**
 * Typedef class for {@link AnalysisService} with {@link IStrategoTerm}.
 */
public class SpoofaxAnalysisService extends AnalysisService<IStrategoTerm, IStrategoTerm> implements
    ISpoofaxAnalysisService {
}
