package org.metaborg.core.processing.parse;

/**
 * Typedef interface for combining {@link IParseResultRequester} and {@link IParseResultUpdater}.
 */
public interface IParseResultProcessor<P> extends IParseResultRequester<P>, IParseResultUpdater<P> {

}
