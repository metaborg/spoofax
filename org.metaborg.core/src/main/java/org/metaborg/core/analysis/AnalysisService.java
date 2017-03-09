package org.metaborg.core.analysis;

import org.metaborg.core.context.IContext;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.syntax.IParseUnit;
import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.LoggerUtils;
import org.metaborg.util.task.ICancel;
import org.metaborg.util.task.IProgress;

public class AnalysisService<P extends IParseUnit, A extends IAnalyzeUnit, AU extends IAnalyzeUnitUpdate>
    implements IAnalysisService<P, A, AU> {
    private static final ILogger logger = LoggerUtils.logger(AnalysisService.class);


    @Override public boolean available(ILanguageImpl langImpl) {
        return langImpl.hasFacet(AnalyzerFacet.class);
    }

    @Override public IAnalyzeResult<A, AU> analyze(P input, IContext context, IProgress progress, ICancel cancel)
        throws AnalysisException, InterruptedException {
        final ILanguageImpl langImpl = context.language();
        final AnalyzerFacet<P, A, AU> facet = facet(langImpl);
        if(facet == null) {
            final String message = logger.format("Cannot get an analyzer for {}", langImpl);
            throw new AnalysisException(context, message);
        }
        final IAnalyzer<P, A, AU> analyzer = facet.analyzer;

        return analyzer.analyze(input, context, progress, cancel);
    }

    @Override public IAnalyzeResults<A, AU> analyzeAll(Iterable<P> inputs, IContext context, IProgress progress,
        ICancel cancel) throws AnalysisException, InterruptedException {
        final ILanguageImpl langImpl = context.language();
        final AnalyzerFacet<P, A, AU> facet = facet(langImpl);
        if(facet == null) {
            final String message = logger.format("Cannot get an analyzer for {}", langImpl);
            throw new AnalysisException(context, message);
        }
        final IAnalyzer<P, A, AU> analyzer = facet.analyzer;

        return analyzer.analyzeAll(inputs, context, progress, cancel);
    }


    @SuppressWarnings("unchecked") private AnalyzerFacet<P, A, AU> facet(ILanguageImpl langImpl) {
        return langImpl.facet(AnalyzerFacet.class);
    }
}
