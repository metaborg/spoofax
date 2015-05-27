package org.metaborg.spoofax.core.processing;

import org.spoofax.interpreter.terms.IStrategoTerm;

/**
 * Typedef interface for {@link IAnalysisResultProcessor} with {@link IStrategoTerm}.
 */
public interface ISpoofaxAnalysisResultProcessor extends IAnalysisResultProcessor<IStrategoTerm, IStrategoTerm>,
    ISpoofaxAnalysisResultRequester, ISpoofaxAnalysisResultUpdater {

}
