package org.metaborg.core.transform;

import java.util.ArrayList;
import java.util.Collection;

import org.metaborg.core.action.IActionService;
import org.metaborg.core.action.ITransformGoal;
import org.metaborg.core.action.TransformActionContrib;
import org.metaborg.core.analysis.IAnalysisService;
import org.metaborg.core.analysis.IAnalyzeUnit;
import org.metaborg.core.context.IContext;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.language.LanguageIdentifier;
import org.metaborg.core.syntax.IParseUnit;
import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.LoggerUtils;


public class TransformService<P extends IParseUnit, A extends IAnalyzeUnit, TP extends ITransformUnit<P>, TA extends ITransformUnit<A>>
    implements ITransformService<P, A, TP, TA> {
    private static final ILogger logger = LoggerUtils.logger(TransformService.class);

    private final IActionService actionService;
    private final IAnalysisService<P, A, ?> analysisService;
    private final ITransformer<P, A, TP, TA> transformer;


    @jakarta.inject.Inject @javax.inject.Inject public TransformService(IActionService actionService, IAnalysisService<P, A, ?> analysisService,
            ITransformer<P, A, TP, TA> transformer) {
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


    @Override public Collection<TP> transform(P input, IContext context, ITransformGoal goal, ITransformConfig config)
        throws TransformException {
        if(!input.valid()) {
            throw new TransformException("Cannot transform parse unit " + input + ", it is not valid");
        }

        final Iterable<TransformActionContrib> actions = actionService.actionContributions(context.language(), goal);
        final Collection<TP> results = new ArrayList<>();
        for(TransformActionContrib action : actions) {
            if(analysisService.available(context.language()))
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
        if(analysisService.available(context.language()))
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
        final Collection<TA> results = new ArrayList<>();
        for(TransformActionContrib action : actions) {
            if (!isActionEnabled(action, context)) {
                // Skip action because it is not enabled by the project or a compile dependency.
                continue;
            }
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
        final Collection<TP> results = new ArrayList<>();
        for(TransformActionContrib action : actions) {
            if(analysisService.available(context.language()))
                checkAnalyzed(action);
            final Collection<TP> result = transformer.transformAllParsed(inputs, context, action, config);
            results.addAll(result);
        }
        return results;
    }

    @Override public Collection<TP> transformAllParsed(Iterable<P> inputs, IContext context,
        TransformActionContrib action, ITransformConfig config) throws TransformException {
        if(analysisService.available(context.language()))
            checkAnalyzed(action);
        final Collection<TP> result = transformer.transformAllParsed(inputs, context, action, config);
        return result;
    }

    @Override public Collection<TA> transformAllAnalyzed(Iterable<A> inputs, IContext context, ITransformGoal goal,
        ITransformConfig config) throws TransformException {
        final Iterable<TransformActionContrib> actions = actionService.actionContributions(context.language(), goal);
        final Collection<TA> results = new ArrayList<>();
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

    /**
     * Determines whether a transformation action is enabled.
     *
     * A transformation action is enabled when it is contributed by a language component that
     * is a compile dependency of this project, or when it is contributed by the project itself.
     *
     * @param action the action to check
     * @param context the context in which to check
     * @return {@code true} when the action is enabled; otherwise, {@code false}
     */
    private static boolean isActionEnabled(TransformActionContrib action, IContext context) {
        // @formatter:off
        final LanguageIdentifier actionContributorId = action.contributor.id();
        return context.project().config().compileDeps().contains(actionContributorId)
            || context.language().id().equals(actionContributorId);
        // @formatter:on
    }
}
