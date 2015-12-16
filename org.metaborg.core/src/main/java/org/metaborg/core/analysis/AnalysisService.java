package org.metaborg.core.analysis;

import org.metaborg.core.context.IContext;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.syntax.ParseResult;
import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.LoggerUtils;

public class AnalysisService<P, A> implements IAnalysisService<P, A> {
    private static final ILogger logger = LoggerUtils.logger(AnalysisService.class);


    @Override public AnalysisResult<P, A> analyze(Iterable<ParseResult<P>> inputs, IContext context)
        throws AnalysisException {
        final ILanguageImpl language = context.language();
        @SuppressWarnings("unchecked") final AnalyzerFacet<P, A> facet = language.facet(AnalyzerFacet.class);
        if(facet == null) {
            logger.debug("No analysis required for {}", language);
            return new AnalysisResult<P, A>(context);
        }
        final IAnalyzer<P, A> analyzer = facet.analyzer;

        return analyzer.analyze(inputs, context);
    }
}
