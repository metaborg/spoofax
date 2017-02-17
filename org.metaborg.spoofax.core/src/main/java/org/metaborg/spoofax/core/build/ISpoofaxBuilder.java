package org.metaborg.spoofax.core.build;

import org.metaborg.core.build.BuildInput;
import org.metaborg.core.build.IBuilder;
import org.metaborg.core.processing.ICancellationToken;
import org.metaborg.core.processing.IProgressReporter;
import org.metaborg.spoofax.core.unit.ISpoofaxAnalyzeUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxAnalyzeUnitUpdate;
import org.metaborg.spoofax.core.unit.ISpoofaxParseUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxTransformUnit;

/**
 * Typedef interface for {@link IBuilder} with Spoofax interfaces.
 */
public interface ISpoofaxBuilder
    extends IBuilder<ISpoofaxParseUnit, ISpoofaxAnalyzeUnit, ISpoofaxAnalyzeUnitUpdate, ISpoofaxTransformUnit<?>> {
    @Override ISpoofaxBuildOutput build(BuildInput input, IProgressReporter progressReporter,
        ICancellationToken cancellationToken) throws InterruptedException;

    @Override ISpoofaxBuildOutput build(BuildInput input) throws InterruptedException;
}
