package org.metaborg.spoofax.core.processing;

import org.metaborg.core.processing.IProcessorRunner;
import org.metaborg.spoofax.core.unit.ISpoofaxAnalyzeUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxParseUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxTransformUnit;

/**
 * Typedef interface for {@link IProcessorRunner} with Spoofax interfaces.
 */
public interface ISpoofaxProcessorRunner
    extends IProcessorRunner<ISpoofaxParseUnit, ISpoofaxAnalyzeUnit, ISpoofaxTransformUnit<?>> {

}
