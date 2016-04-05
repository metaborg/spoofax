package org.metaborg.spoofax.core.analysis;

import org.metaborg.core.analysis.AnalysisException;
import org.metaborg.core.analysis.IAnalysisService;
import org.metaborg.core.context.IContext;
import org.metaborg.spoofax.core.unit.ISpoofaxAnalyzeUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxAnalyzeUnitUpdate;
import org.metaborg.spoofax.core.unit.ISpoofaxParseUnit;

/**
 * Typedef interface for {@link IAnalysisService} with Spoofax interfaces.
 */
public interface ISpoofaxAnalysisService
    extends IAnalysisService<ISpoofaxParseUnit, ISpoofaxAnalyzeUnit, ISpoofaxAnalyzeUnitUpdate> {
    /**
     * {@inheritDoc}
     */
    @Override ISpoofaxAnalyzeResult analyze(ISpoofaxParseUnit input, IContext context) throws AnalysisException;

    /**
     * {@inheritDoc}
     */
    @Override ISpoofaxAnalyzeResults analyzeAll(Iterable<ISpoofaxParseUnit> inputs, IContext context)
        throws AnalysisException;
}
