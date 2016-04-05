package org.metaborg.core.analysis;

import org.metaborg.core.context.IContext;
import org.metaborg.core.syntax.IParseUnit;

/**
 * Interface for semantic analysis of parsed files, and retrieving origin information of analyzed fragments.
 */
public interface IAnalyzer<P extends IParseUnit, A extends IAnalyzeUnit, AU extends IAnalyzeUnitUpdate> {
    IAnalyzeResult<A, AU> analyze(P input, IContext context) throws AnalysisException;

    IAnalyzeResults<A, AU> analyzeAll(Iterable<P> inputs, IContext context) throws AnalysisException;
}
