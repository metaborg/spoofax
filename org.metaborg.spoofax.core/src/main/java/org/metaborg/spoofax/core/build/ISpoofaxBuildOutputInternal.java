package org.metaborg.spoofax.core.build;

import org.metaborg.core.build.IBuildOutputInternal;
import org.metaborg.spoofax.core.unit.ISpoofaxAnalyzeUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxAnalyzeUnitUpdate;
import org.metaborg.spoofax.core.unit.ISpoofaxParseUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxTransformUnit;

/**
 * Typedef interface for {@link IBuildOutputInternal} with Spoofax interfaces.
 */
public interface ISpoofaxBuildOutputInternal extends
    IBuildOutputInternal<ISpoofaxParseUnit, ISpoofaxAnalyzeUnit, ISpoofaxAnalyzeUnitUpdate, ISpoofaxTransformUnit<?>>,
    ISpoofaxBuildOutput {

}
