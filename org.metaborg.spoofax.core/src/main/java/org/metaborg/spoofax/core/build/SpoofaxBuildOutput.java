package org.metaborg.spoofax.core.build;

import org.metaborg.core.build.BuildOutput;
import org.metaborg.spoofax.core.unit.ISpoofaxAnalyzeUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxAnalyzeUnitUpdate;
import org.metaborg.spoofax.core.unit.ISpoofaxParseUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxTransformUnit;

/**
 * Typedef class for {@link BuildOutput} with Spoofax interfaces.
 */
public class SpoofaxBuildOutput
    extends BuildOutput<ISpoofaxParseUnit, ISpoofaxAnalyzeUnit, ISpoofaxAnalyzeUnitUpdate, ISpoofaxTransformUnit<?>>
    implements ISpoofaxBuildOutputInternal {

}
