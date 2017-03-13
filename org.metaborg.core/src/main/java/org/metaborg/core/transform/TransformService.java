package org.metaborg.core.transform;

import java.util.Collection;

import org.metaborg.core.action.IActionService;
import org.metaborg.core.action.ITransformGoal;
import org.metaborg.core.action.TransformActionContrib;
import org.metaborg.core.analysis.IAnalyzeUnit;
import org.metaborg.core.context.IContext;
import org.metaborg.core.syntax.IParseUnit;
import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.LoggerUtils;

import com.google.common.collect.Lists;
import com.google.inject.Inject;

public class TransformService<P extends IParseUnit, A extends IAnalyzeUnit, TP extends ITransformUnit<P>, TA extends ITransformUnit<A>>
    implements ITransformService<P, A, TP, TA> {
    private static final ILogger logger = LoggerUtils.logger(TransformService.class);

    private final IActionService actionService;
    private final ITransformer<P, A, TP, TA> transformer;


    @Inject public TransformService(IActionService actionService, ITransformer<P, A, TP, TA> transformer) {
        this.actionService = actionService;
        this.transformer = transformer;
    }


    @Override public boolean available(IContext context, ITransformGoal goal) {
        return actionService.available(context.language(), goal);
    }

    @Override public boolean requiresAnalysis(IContext context, ITransformGoal goal) {
        return actionService.requiresAnalysis(context.language(), goal);
    }


    @Override public Collection<TP> transform(P input, IContext context, ITransformGoal goal, ITransformConfig config)
        throws TransformException {
        if(!input.valid()) {
            throw new TransformException("Cannot transform parse unit " + input + ", it is not valid");
        }

        final Iterable<TransformActionContrib> actions = actionService.actionContributions(context.language(), goal);
        final Collection<TP> results = Lists.newArrayList();
        for(TransformActionContrib action : actions) {
            checkAnalyzed(action);
            final TP result = transformer.transform(input, context, action, config);
            results.add(result);
        }
        return results;
    }

    @Override public TP transform(P input, IContext context, TransformActionContrib action, ITransformConfig config)
        throws TransformException {
        if(!input.valid()) {
            throw new TransformException("Cannot transform parse unit " + input + ", it is not valid");
        }
        checkAnalyzed(action);

        final TP result = transformer.transform(input, context, action, config);
        return result;
    }

    @Override public Collection<TA> transform(A input, IContext context, ITransformGoal goal, ITransformConfig config)
        throws TransformException {
        if(!input.valid()) {
            throw new TransformException("Cannot transform analyze unit " + input + ", it is not valid");
        }

        final Iterable<TransformActionContrib> actions = actionService.actionContributions(context.language(), goal);
        final Collection<TA> results = Lists.newArrayList();
        for(TransformActionContrib action : actions) {
            final TA result = transformer.transform(input, context, action, config);
            results.add(result);
        }
        return results;
    }

    @Override public TA transform(A input, IContext context, TransformActionContrib action, ITransformConfig config)
        throws TransformException {
        if(!input.valid()) {
            throw new TransformException("Cannot transform parse unit " + input + ", it is not valid");
        }

        final TA result = transformer.transform(input, context, action, config);
        return result;
    }



    @Override public Collection<TP> transformAllParsed(Iterable<P> inputs, IContext context, ITransformGoal goal,
        ITransformConfig config) throws TransformException {
        final Iterable<TransformActionContrib> actions = actionService.actionContributions(context.language(), goal);
        final Collection<TP> results = Lists.newArrayList();
        for(TransformActionContrib action : actions) {
            checkAnalyzed(action);
            final Collection<TP> result = transformer.transformAllParsed(inputs, context, action, config);
            results.addAll(result);
        }
        return results;
    }

    @Override public Collection<TP> transformAllParsed(Iterable<P> inputs, IContext context,
        TransformActionContrib action, ITransformConfig config) throws TransformException {
        checkAnalyzed(action);
        final Collection<TP> result = transformer.transformAllParsed(inputs, context, action, config);
        return result;
    }

    @Override public Collection<TA> transformAllAnalyzed(Iterable<A> inputs, IContext context, ITransformGoal goal,
        ITransformConfig config) throws TransformException {
        final Iterable<TransformActionContrib> actions = actionService.actionContributions(context.language(), goal);
        final Collection<TA> results = Lists.newArrayList();
        for(TransformActionContrib action : actions) {
            final Collection<TA> result = transformer.transformAllAnalyzed(inputs, context, action, config);
            results.addAll(result);
        }
        return results;
    }

    @Override public Collection<TA> transformAllAnalyzed(Iterable<A> inputs, IContext context,
        TransformActionContrib action, ITransformConfig config) throws TransformException {
        final Collection<TA> result = transformer.transformAllAnalyzed(inputs, context, action, config);
        return result;
    }


    private static void checkAnalyzed(TransformActionContrib action) throws TransformException {
        if(!action.action.flags().parsed) {
            final String message =
                logger.format("Transformation {} requires an analyzed result, but a parsed result is given", action);
            throw new TransformException(message);
        }
    }
}
