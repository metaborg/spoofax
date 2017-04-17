package org.metaborg.spoofax.core.build;

import org.metaborg.core.build.BuildInput;
import org.metaborg.core.build.IBuilder;
import org.metaborg.spoofax.core.unit.ISpoofaxAnalyzeUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxAnalyzeUnitUpdate;
import org.metaborg.spoofax.core.unit.ISpoofaxParseUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxTransformUnit;
import org.metaborg.util.task.ICancel;
import org.metaborg.util.task.IProgress;

/**
 * Typedef interface for {@link IBuilder} with Spoofax interfaces.
 */
public interface ISpoofaxBuilder
    extends IBuilder<ISpoofaxParseUnit, ISpoofaxAnalyzeUnit, ISpoofaxAnalyzeUnitUpdate, ISpoofaxTransformUnit<?>> {
    @Override ISpoofaxBuildOutput build(BuildInput input, IProgress progress, ICancel cancel)
        throws InterruptedException;

    @Override ISpoofaxBuildOutput build(BuildInput input) throws InterruptedException;
}
