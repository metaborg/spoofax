package org.metaborg.spoofax.core.build.processing.analyze;

import org.metaborg.core.build.processing.analyze.IAnalysisResultRequester;
import org.spoofax.interpreter.terms.IStrategoTerm;

/**
 * Typedef interface for {@link IAnalysisResultRequester} with {@link IStrategoTerm}.
 */
public interface ISpoofaxAnalysisResultRequester extends IAnalysisResultRequester<IStrategoTerm, IStrategoTerm> {

}
