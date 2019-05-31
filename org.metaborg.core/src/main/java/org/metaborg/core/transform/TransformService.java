package org.metaborg.core.transform;

import java.util.Collection;

import org.metaborg.core.action.IActionService;
import org.metaborg.core.action.ITransformAction;
import org.metaborg.core.action.ITransformGoal;
import org.metaborg.core.action.TransformActionContrib;
import org.metaborg.core.analysis.IAnalysisService;
import org.metaborg.core.analysis.IAnalyzeUnit;
import org.metaborg.core.context.IContext;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.syntax.IParseUnit;
import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.LoggerUtils;

import com.google.common.collect.Lists;
import com.google.inject.Inject;

public class TransformService<P extends IParseUnit, A extends IAnalyzeUnit, TUP extends ITransformUnit<P>, TUA extends ITransformUnit<A>, TA extends ITransformAction>
    implements ITransformService<P, A, TUP, TUA, TA> {
    private static final ILogger logger = LoggerUtils.logger(TransformService.class);

    private final IActionService<TA> actionService;
    private final IAnalysisService<P, A, ?> analysisService;
    private final ITransformer<P, A, TUP, TUA, TA> transformer;


    @Inject public TransformService(IActionService<TA> actionService, IAnalysisService<P, A, ?> analysisService,
        ITransformer<P, A, TUP, TUA, TA> transformer) {
        this.actionService = actionService;
        this.analysisService = analysisService;
        this.transformer = transformer;
    }


    @Override public boolean available(ILanguageImpl language, ITransformGoal goal) {
        return actionService.available(language, goal);
    }

    @Override public boolean requiresAnalysis(ILanguageImpl language, ITransformGoal goal) {
        return actionService.requiresAnalysis(language, goal);
    }


    @Override public Collection<TUP> transform(P input, IContext context, ITransformGoal goal, ITransformConfig config)
        throws TransformException {
        if(!input.valid()) {
            throw new TransformException("Cannot transform parse unit " + input + ", it is not valid");
        }

        final Iterable<TransformActionContrib<TA>> actions =
            actionService.actionContributions(context.language(), goal);
        final Collection<TUP> results = Lists.newArrayList();
        for(TransformActionContrib<TA> action : actions) {
            if(analysisService.available(context.language()))
                checkAnalyzed(action);
            final TUP result = transformer.transform(input, context, action, config);
            results.add(result);
        }
        return results;
    }

    @Override public TUP transform(P input, IContext context, TransformActionContrib<TA> action,
        ITransformConfig config) throws TransformException {
        if(!input.valid()) {
            throw new TransformException("Cannot transform parse unit " + input + ", it is not valid");
        }
        if(analysisService.available(context.language()))
            checkAnalyzed(action);

        final TUP result = transformer.transform(input, context, action, config);
        return result;
    }

    @Override public Collection<TUA> transform(A input, IContext context, ITransformGoal goal, ITransformConfig config)
        throws TransformException {
        if(!input.valid()) {
            throw new TransformException("Cannot transform analyze unit " + input + ", it is not valid");
        }

        final Iterable<TransformActionContrib<TA>> actions =
            actionService.actionContributions(context.language(), goal);
        final Collection<TUA> results = Lists.newArrayList();
        for(TransformActionContrib<TA> action : actions) {
            final TUA result = transformer.transform(input, context, action, config);
            results.add(result);
        }
        return results;
    }

    @Override public TUA transform(A input, IContext context, TransformActionContrib<TA> action,
        ITransformConfig config) throws TransformException {
        if(!input.valid()) {
            throw new TransformException("Cannot transform parse unit " + input + ", it is not valid");
        }

        final TUA result = transformer.transform(input, context, action, config);
        return result;
    }



    @Override public Collection<TUP> transformAllParsed(Iterable<P> inputs, IContext context, ITransformGoal goal,
        ITransformConfig config) throws TransformException {
        final Iterable<TransformActionContrib<TA>> actions =
            actionService.actionContributions(context.language(), goal);
        final Collection<TUP> results = Lists.newArrayList();
        for(TransformActionContrib<TA> action : actions) {
            if(analysisService.available(context.language()))
                checkAnalyzed(action);
            final Collection<TUP> result = transformer.transformAllParsed(inputs, context, action, config);
            results.addAll(result);
        }
        return results;
    }

    @Override public Collection<TUP> transformAllParsed(Iterable<P> inputs, IContext context,
        TransformActionContrib<TA> action, ITransformConfig config) throws TransformException {
        if(analysisService.available(context.language()))
            checkAnalyzed(action);
        final Collection<TUP> result = transformer.transformAllParsed(inputs, context, action, config);
        return result;
    }

    @Override public Collection<TUA> transformAllAnalyzed(Iterable<A> inputs, IContext context, ITransformGoal goal,
        ITransformConfig config) throws TransformException {
        final Iterable<TransformActionContrib<TA>> actions =
            actionService.actionContributions(context.language(), goal);
        final Collection<TUA> results = Lists.newArrayList();
        for(TransformActionContrib<TA> action : actions) {
            final Collection<TUA> result = transformer.transformAllAnalyzed(inputs, context, action, config);
            results.addAll(result);
        }
        return results;
    }

    @Override public Collection<TUA> transformAllAnalyzed(Iterable<A> inputs, IContext context,
        TransformActionContrib<TA> action, ITransformConfig config) throws TransformException {
        final Collection<TUA> result = transformer.transformAllAnalyzed(inputs, context, action, config);
        return result;
    }


    private static <TA extends ITransformAction> void checkAnalyzed(TransformActionContrib<TA> actionContrib)
        throws TransformException {
        if(!actionContrib.action.flags().parsed) {
            final String message =
                logger.format("Transformation {} requires an analyzed result, but a parsed result is given", actionContrib);
            throw new TransformException(message);
        }
    }
}
