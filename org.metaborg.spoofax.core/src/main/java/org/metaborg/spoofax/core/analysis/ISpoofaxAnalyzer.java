package org.metaborg.spoofax.core.analysis;

import org.metaborg.core.analysis.AnalysisException;
import org.metaborg.core.analysis.IAnalyzer;
import org.metaborg.core.context.IContext;
import org.metaborg.spoofax.core.unit.ISpoofaxAnalyzeUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxAnalyzeUnitUpdate;
import org.metaborg.spoofax.core.unit.ISpoofaxParseUnit;
import org.metaborg.util.task.ICancel;
import org.metaborg.util.task.IProgress;

/**
 * Typedef interface for {@link IAnalyzer} with Spoofax interfaces.
 */
public interface ISpoofaxAnalyzer extends IAnalyzer<ISpoofaxParseUnit, ISpoofaxAnalyzeUnit, ISpoofaxAnalyzeUnitUpdate> {
    /**
     * {@inheritDoc}
     */
    @Override ISpoofaxAnalyzeResult analyze(ISpoofaxParseUnit input, IContext context, IProgress progress,
        ICancel cancel) throws AnalysisException, InterruptedException;

    /**
     * {@inheritDoc}
     */
    @Override ISpoofaxAnalyzeResults analyzeAll(Iterable<ISpoofaxParseUnit> inputs, IContext context,
        IProgress progress, ICancel cancel) throws AnalysisException, InterruptedException;
}
