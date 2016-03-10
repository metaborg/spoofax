package org.metaborg.core.analysis;

import java.util.Collection;

import org.metaborg.core.context.IContext;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.syntax.IParseUnit;
import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.LoggerUtils;

public class AnalysisService<P extends IParseUnit, A extends IAnalyzeUnit> implements IAnalysisService<P, A> {
    private static final ILogger logger = LoggerUtils.logger(AnalysisService.class);


    @Override public boolean available(ILanguageImpl langImpl) {
        return langImpl.hasFacet(AnalyzerFacet.class);
    }

    @Override public A analyze(P input, IContext context) throws AnalysisException {
        final ILanguageImpl langImpl = context.language();
        final AnalyzerFacet<P, A> facet = facet(langImpl);
        if(facet == null) {
            final String message = logger.format("Cannot get an analyzer for {}", langImpl);
            throw new AnalysisException(context, message);
        }
        final IAnalyzer<P, A> analyzer = facet.analyzer;

        return analyzer.analyze(input, context);
    }

    @Override public Collection<A> analyzeAll(Iterable<P> inputs, IContext context) throws AnalysisException {
        final ILanguageImpl langImpl = context.language();
        final AnalyzerFacet<P, A> facet = facet(langImpl);
        if(facet == null) {
            final String message = logger.format("Cannot get an analyzer for {}", langImpl);
            throw new AnalysisException(context, message);
        }
        final IAnalyzer<P, A> analyzer = facet.analyzer;

        return analyzer.analyzeAll(inputs, context);
    }


    @SuppressWarnings("unchecked") private AnalyzerFacet<P, A> facet(ILanguageImpl langImpl) {
        return langImpl.facet(AnalyzerFacet.class);
    }
}
