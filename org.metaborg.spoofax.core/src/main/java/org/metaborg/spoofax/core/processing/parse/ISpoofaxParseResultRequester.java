package org.metaborg.spoofax.core.processing.parse;

import org.metaborg.core.processing.parse.IParseResultRequester;
import org.metaborg.spoofax.core.unit.ISpoofaxInputUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxParseUnit;

/**
 * Typedef interface for {@link IParseResultRequester} with Spoofax interfaces.
 */
public interface ISpoofaxParseResultRequester extends IParseResultRequester<ISpoofaxInputUnit, ISpoofaxParseUnit> {

}
