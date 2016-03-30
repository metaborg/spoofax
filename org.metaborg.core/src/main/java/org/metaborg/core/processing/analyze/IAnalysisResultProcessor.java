package org.metaborg.core.processing.analyze;

import org.metaborg.core.analysis.IAnalyzeUnit;
import org.metaborg.core.syntax.IInputUnit;
import org.metaborg.core.syntax.IParseUnit;

/**
 * Typedef interface for combining {@link IAnalysisResultRequester} and {@link IAnalysisResultUpdater}.
 */
public interface IAnalysisResultProcessor<I extends IInputUnit, P extends IParseUnit, A extends IAnalyzeUnit>
    extends IAnalysisResultRequester<I, A>, IAnalysisResultUpdater<P, A> {

}
