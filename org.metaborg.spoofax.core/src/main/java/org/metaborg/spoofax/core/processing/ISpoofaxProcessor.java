package org.metaborg.spoofax.core.processing;

import org.metaborg.core.processing.IProcessor;
import org.metaborg.spoofax.core.unit.ISpoofaxAnalyzeUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxParseUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxTransformUnit;

/**
 * Typedef interface for {@link IProcessor} with Spoofax interfaces.
 */
public interface ISpoofaxProcessor
    extends IProcessor<ISpoofaxParseUnit, ISpoofaxAnalyzeUnit, ISpoofaxTransformUnit<?>> {

}
