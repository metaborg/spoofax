package org.metaborg.spoofax.core.transform;

import java.util.Map;

import org.metaborg.spoofax.core.analysis.AnalysisFileResult;
import org.metaborg.spoofax.core.context.IContext;
import org.metaborg.spoofax.core.syntax.ParseResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

/**
 * Transformer that uses Stratego
 */
public class Transformer<ParseT, AnalysisT, TransT> implements ITransformer<ParseT, AnalysisT, TransT> {
    private static final Logger logger = LoggerFactory.getLogger(Transformer.class);

    private final Map<Class<? extends ITransformerGoal>, ITransformerExecutor<ParseT, AnalysisT, TransT>> executors;
    private final Map<Class<? extends ITransformerGoal>, ITransformerResultHandler<TransT>> resultHandlers;


    @Inject public Transformer(
        Map<Class<? extends ITransformerGoal>, ITransformerExecutor<ParseT, AnalysisT, TransT>> executors,
        Map<Class<? extends ITransformerGoal>, ITransformerResultHandler<TransT>> resultHandlers) {
        this.executors = executors;
        this.resultHandlers = resultHandlers;
    }


    @Override public TransformResult<ParseResult<ParseT>, TransT> transform(ParseResult<ParseT> parseResult,
        IContext context, ITransformerGoal goal) throws TransformerException {
        final ITransformerExecutor<ParseT, AnalysisT, TransT> executor = executor(goal);
        final TransformResult<ParseResult<ParseT>, TransT> result = executor.transform(parseResult, context, goal);
        handleResult(result, goal);
        return result;
    }

    @Override public TransformResult<AnalysisFileResult<ParseT, AnalysisT>, TransT> transform(
        AnalysisFileResult<ParseT, AnalysisT> analysisResult, IContext context, ITransformerGoal goal)
        throws TransformerException {
        final ITransformerExecutor<ParseT, AnalysisT, TransT> executor = executor(goal);
        final TransformResult<AnalysisFileResult<ParseT, AnalysisT>, TransT> result =
            executor.transform(analysisResult, context, goal);
        handleResult(result, goal);
        return result;
    }


    @Override public boolean available(ITransformerGoal goal, IContext context) {
        try {
            final ITransformerExecutor<ParseT, AnalysisT, TransT> executor = executor(goal);
            return executor.available(goal, context);
        } catch(TransformerException e) {
            return false;
        }
    }


    private ITransformerExecutor<ParseT, AnalysisT, TransT> executor(ITransformerGoal goal) throws TransformerException {
        final ITransformerExecutor<ParseT, AnalysisT, TransT> executor = executors.get(goal.getClass());
        if(executor == null) {
            final String message = String.format("No executor for %s", goal);
            logger.error(message);
            throw new TransformerException(message);
        }
        return executor;
    }

    private void handleResult(TransformResult<?, TransT> result, ITransformerGoal goal) {
        final ITransformerResultHandler<TransT> resultHandler = resultHandlers.get(goal.getClass());
        resultHandler.handle(result, goal);
    }
}
