package org.metaborg.spoofax.core.build.processing.analyze;

import org.metaborg.core.build.processing.analyze.IAnalysisResultProcessor;
import org.spoofax.interpreter.terms.IStrategoTerm;

/**
 * Typedef interface for {@link IAnalysisResultProcessor} with {@link IStrategoTerm}.
 */
public interface ISpoofaxAnalysisResultProcessor extends IAnalysisResultProcessor<IStrategoTerm, IStrategoTerm>,
    ISpoofaxAnalysisResultRequester, ISpoofaxAnalysisResultUpdater {

}
