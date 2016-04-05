package org.metaborg.spoofax.core.processing.parse;

import org.metaborg.core.processing.parse.IParseResultProcessor;
import org.metaborg.spoofax.core.unit.ISpoofaxInputUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxParseUnit;

/**
 * Typedef interface for {@link IParseResultProcessor} with Spoofax interfaces.
 */
public interface ISpoofaxParseResultProcessor extends IParseResultProcessor<ISpoofaxInputUnit, ISpoofaxParseUnit>,
    ISpoofaxParseResultRequester, ISpoofaxParseResultUpdater {

}
