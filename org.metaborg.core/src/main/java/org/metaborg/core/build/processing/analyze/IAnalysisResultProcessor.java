package org.metaborg.core.build.processing.analyze;

/**
 * Typedef interface for combining {@link IAnalysisResultRequester} and {@link IAnalysisResultUpdater}.
 */
public interface IAnalysisResultProcessor<P, A> extends IAnalysisResultRequester<P, A>, IAnalysisResultUpdater<P, A> {

}
