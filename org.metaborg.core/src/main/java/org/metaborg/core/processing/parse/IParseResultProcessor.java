package org.metaborg.core.processing.parse;

import org.metaborg.core.syntax.IInputUnit;
import org.metaborg.core.syntax.IParseUnit;

/**
 * Typedef interface for combining {@link IParseResultRequester} and {@link IParseResultUpdater}.
 */
public interface IParseResultProcessor<I extends IInputUnit, P extends IParseUnit>
    extends IParseResultRequester<I, P>, IParseResultUpdater<P> {

}
