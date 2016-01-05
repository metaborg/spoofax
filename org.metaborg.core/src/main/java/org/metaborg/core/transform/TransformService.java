package org.metaborg.core.transform;

import java.util.Collection;

import org.metaborg.core.action.IActionService;
import org.metaborg.core.action.ITransformGoal;
import org.metaborg.core.action.TransformActionContribution;
import org.metaborg.core.analysis.AnalysisFileResult;
import org.metaborg.core.context.IContext;
import org.metaborg.core.syntax.ParseResult;
import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.LoggerUtils;

import com.google.common.collect.Lists;
import com.google.inject.Inject;

public class TransformService<P, A, T> implements ITransformService<P, A, T> {
    private static final ILogger logger = LoggerUtils.logger(TransformService.class);

    private final IActionService actionService;
    private final ITransformer<P, A, T> transformer;


    @Inject public TransformService(IActionService actionService, ITransformer<P, A, T> transformer) {
        this.actionService = actionService;
        this.transformer = transformer;
    }


    @Override public TransformResults<P, T> transform(ParseResult<P> input, IContext context, ITransformGoal goal)
        throws TransformException {
        final Iterable<TransformActionContribution> actions =
            actionService.actionContributions(context.language(), goal);
        final Collection<TransformResult<P, T>> results = Lists.newLinkedList();
        for(TransformActionContribution action : actions) {
            final TransformResult<P, T> result = transformAction(input, context, action);
            results.add(result);
        }
        return new TransformResults<>(results, goal);
    }

    @Override public TransformResults<A, T> transform(AnalysisFileResult<P, A> input, IContext context,
        ITransformGoal goal) throws TransformException {
        final Iterable<TransformActionContribution> actions =
            actionService.actionContributions(context.language(), goal);
        final Collection<TransformResult<A, T>> results = Lists.newLinkedList();
        for(TransformActionContribution action : actions) {
            final TransformResult<A, T> result = transformAction(input, context, action);
            results.add(result);
        }
        return new TransformResults<>(results, goal);
    }

    @Override public boolean available(IContext context, ITransformGoal goal) {
        return actionService.available(context.language(), goal);
    }

    @Override public boolean requiresAnalysis(IContext context, ITransformGoal goal) {
        return actionService.requiresAnalysis(context.language(), goal);
    }


    @Override public TransformResult<P, T> transformAction(ParseResult<P> result, IContext context,
        TransformActionContribution action) throws TransformException {
        if(!action.action.flags().parsed) {
            final String message =
                logger.format("Transformation {} requires an analyzed result, but a parsed result is given", action);
            throw new TransformException(message);
        }
        return transformer.transform(result, context, action);
    }

    @Override public TransformResult<A, T> transformAction(AnalysisFileResult<P, A> result, IContext context,
        TransformActionContribution action) throws TransformException {
        if(action.action.flags().parsed) {
            final TransformResult<P, T> output = transformer.transform(result.previous, context, action);
            return new TransformResult<>(output, result);
        }
        return transformer.transform(result, context, action);
    }
}
