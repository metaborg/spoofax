package org.metaborg.core.analysis;

import java.util.Collection;

import org.metaborg.core.context.IContext;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.syntax.IParseUnit;

/**
 * Interface for semantic analysis of parsed files, and retrieving origin information of analyzed fragments.
 */
public interface IAnalysisService<P extends IParseUnit, A extends IAnalyzeUnit> {
    boolean available(ILanguageImpl langImpl);

    A analyze(P input, IContext context) throws AnalysisException;

    Collection<A> analyzeAll(Iterable<P> inputs, IContext context) throws AnalysisException;
}
