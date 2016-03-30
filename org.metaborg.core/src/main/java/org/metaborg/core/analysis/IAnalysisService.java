package org.metaborg.core.analysis;

import org.metaborg.core.context.IContext;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.syntax.IParseUnit;

/**
 * Interface for semantic analysis of parsed files, and retrieving origin information of analyzed fragments.
 */
public interface IAnalysisService<P extends IParseUnit, A extends IAnalyzeUnit, AU extends IAnalyzeUnitUpdate> {
    boolean available(ILanguageImpl langImpl);

    IAnalyzeResult<A, AU> analyze(P input, IContext context) throws AnalysisException;

    IAnalyzeResults<A, AU> analyzeAll(Iterable<P> inputs, IContext context) throws AnalysisException;
}
