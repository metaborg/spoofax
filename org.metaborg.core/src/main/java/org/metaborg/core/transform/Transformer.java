package org.metaborg.core.transform;

import java.util.Map;

import org.metaborg.core.analysis.AnalysisFileResult;
import org.metaborg.core.context.IContext;
import org.metaborg.core.syntax.ParseResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

/**
 * Transformer that uses Stratego
 */
public class Transformer<P, A, T> implements ITransformer<P, A, T> {
    private static final Logger logger = LoggerFactory.getLogger(Transformer.class);

    private final Map<Class<? extends ITransformerGoal>, ITransformerExecutor<P, A, T>> executors;
    private final Map<Class<? extends ITransformerGoal>, ITransformerResultHandler<T>> resultHandlers;


    @Inject public Transformer(Map<Class<? extends ITransformerGoal>, ITransformerExecutor<P, A, T>> executors,
        Map<Class<? extends ITransformerGoal>, ITransformerResultHandler<T>> resultHandlers) {
        this.executors = executors;
        this.resultHandlers = resultHandlers;
    }


    @Override public TransformResult<ParseResult<P>, T> transform(ParseResult<P> parseResult, IContext context,
        ITransformerGoal goal) throws TransformerException {
        final ITransformerExecutor<P, A, T> executor = executor(goal);
        final TransformResult<ParseResult<P>, T> result = executor.transform(parseResult, context, goal);
        handleResult(result, goal);
        return result;
    }

    @Override public TransformResult<AnalysisFileResult<P, A>, T> transform(AnalysisFileResult<P, A> analysisResult,
        IContext context, ITransformerGoal goal) throws TransformerException {
        final ITransformerExecutor<P, A, T> executor = executor(goal);
        final TransformResult<AnalysisFileResult<P, A>, T> result = executor.transform(analysisResult, context, goal);
        handleResult(result, goal);
        return result;
    }


    @Override public boolean available(ITransformerGoal goal, IContext context) {
        try {
            final ITransformerExecutor<P, A, T> executor = executor(goal);
            return executor.available(goal, context);
        } catch(TransformerException e) {
            return false;
        }
    }


    private ITransformerExecutor<P, A, T> executor(ITransformerGoal goal) throws TransformerException {
        final ITransformerExecutor<P, A, T> executor = executors.get(goal.getClass());
        if(executor == null) {
            final String message = String.format("No executor for %s", goal);
            logger.error(message);
            throw new TransformerException(message);
        }
        return executor;
    }

    private void handleResult(TransformResult<?, T> result, ITransformerGoal goal) {
        final ITransformerResultHandler<T> resultHandler = resultHandlers.get(goal.getClass());
        resultHandler.handle(result, goal);
    }
}
