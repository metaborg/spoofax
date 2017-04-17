package org.metaborg.spoofax.core.analysis;

import org.metaborg.core.MetaborgRuntimeException;
import org.metaborg.core.analysis.AnalysisException;
import org.metaborg.core.analysis.IAnalysisService;
import org.metaborg.core.context.IContext;
import org.metaborg.core.processing.NullCancel;
import org.metaborg.core.processing.NullProgress;
import org.metaborg.spoofax.core.unit.ISpoofaxAnalyzeUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxAnalyzeUnitUpdate;
import org.metaborg.spoofax.core.unit.ISpoofaxParseUnit;
import org.metaborg.util.task.ICancel;
import org.metaborg.util.task.IProgress;

/**
 * Typedef interface for {@link IAnalysisService} with Spoofax interfaces.
 */
public interface ISpoofaxAnalysisService
    extends IAnalysisService<ISpoofaxParseUnit, ISpoofaxAnalyzeUnit, ISpoofaxAnalyzeUnitUpdate> {
    /**
     * {@inheritDoc}
     */
    @Override ISpoofaxAnalyzeResult analyze(ISpoofaxParseUnit input, IContext context, IProgress progress,
        ICancel cancel) throws AnalysisException, InterruptedException;

    /**
     * {@inheritDoc}
     */
    @Override default ISpoofaxAnalyzeResult analyze(ISpoofaxParseUnit input, IContext context)
        throws AnalysisException {
        try {
            return analyze(input, context, new NullProgress(), new NullCancel());
        } catch(InterruptedException e) {
            // This cannot happen, since we pass a null cancellation token, but we need to handle the exception.
            throw new MetaborgRuntimeException("Interrupted", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override ISpoofaxAnalyzeResults analyzeAll(Iterable<ISpoofaxParseUnit> inputs, IContext context,
        IProgress progress, ICancel cancel) throws AnalysisException, InterruptedException;

    /**
     * {@inheritDoc}
     */
    @Override default ISpoofaxAnalyzeResults analyzeAll(Iterable<ISpoofaxParseUnit> inputs, IContext context)
        throws AnalysisException {
        try {
            return analyzeAll(inputs, context, new NullProgress(), new NullCancel());
        } catch(InterruptedException e) {
            // This cannot happen, since we pass a null cancellation token, but we need to handle the exception.
            throw new MetaborgRuntimeException("Interrupted", e);
        }
    }
}
