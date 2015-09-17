package org.metaborg.core.analysis;

import java.util.Map;

import org.metaborg.core.context.IContext;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.syntax.ParseResult;
import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.LoggerUtils;

import com.google.inject.Inject;

public class AnalysisService<P, A> implements IAnalysisService<P, A> {
    private static final ILogger logger = LoggerUtils.logger(AnalysisService.class);

    private final Map<String, IAnalyzer<P, A>> analyzers;


    @Inject public AnalysisService(Map<String, IAnalyzer<P, A>> analyzers) {
        this.analyzers = analyzers;
    }


    @Override public AnalysisResult<P, A> analyze(Iterable<ParseResult<P>> inputs, IContext context)
        throws AnalysisException {
        final ILanguageImpl language = context.language();
        final AnalyzerFacet facet = language.facet(AnalyzerFacet.class);
        if(facet == null) {
            logger.debug("No analysis required for {}", language);
            return new AnalysisResult<P, A>(context);
        }
        final String analyzerName = facet.analyzerName;

        final IAnalyzer<P, A> analyzer = analyzers.get(analyzerName);
        if(analyzer == null) {
            final String message =
                logger.format("Cannot analyze inputs of {}, analyzer with name {} does not exist", language,
                    analyzerName);
            throw new AnalysisException(context, message);
        }

        return analyzer.analyze(inputs, context);
    }
}
