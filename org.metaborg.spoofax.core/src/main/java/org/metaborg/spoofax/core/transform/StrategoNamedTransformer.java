package org.metaborg.spoofax.core.transform;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.MetaborgException;
import org.metaborg.core.MetaborgRuntimeException;
import org.metaborg.core.analysis.AnalysisFileResult;
import org.metaborg.core.context.IContext;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.syntax.ParseResult;
import org.metaborg.core.transform.ITransformerGoal;
import org.metaborg.core.transform.NamedGoal;
import org.metaborg.core.transform.TransformResult;
import org.metaborg.core.transform.TransformerException;
import org.metaborg.spoofax.core.transform.menu.Action;
import org.metaborg.spoofax.core.transform.menu.MenusFacet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spoofax.interpreter.terms.IStrategoTerm;

import com.google.inject.Inject;

/**
 * Transformer executor for the {@link NamedGoal} and {@link MenusFacet}.
 */
public class StrategoNamedTransformer implements IStrategoTransformerExecutor {
    private static final Logger logger = LoggerFactory.getLogger(StrategoNamedTransformer.class);

    private final StrategoTransformerCommon transformer;


    @Inject public StrategoNamedTransformer(StrategoTransformerCommon transformer) {
        this.transformer = transformer;
    }


    @Override public TransformResult<ParseResult<IStrategoTerm>, IStrategoTerm> transform(
        ParseResult<IStrategoTerm> parseResult, IContext context, ITransformerGoal goal) throws TransformerException {
        final Action action = action(context.language(), goal);
        final FileObject resource = parseResult.source;
        try {
            final IStrategoTerm inputTerm =
                transformer.builderInputTerm(parseResult.result, resource, context.location());
            logger.debug("Transforming parse result of {} with '{}'", resource, action.name);
            return transformer.transform(context, parseResult, action.strategy, inputTerm, resource);
        } catch(TransformerException e) {
            throw e;
        } catch(MetaborgException e) {
            throw new TransformerException("Cannot create input term", e);
        }
    }

    @Override public TransformResult<AnalysisFileResult<IStrategoTerm, IStrategoTerm>, IStrategoTerm> transform(
        AnalysisFileResult<IStrategoTerm, IStrategoTerm> analysisResult, IContext context, ITransformerGoal goal)
        throws TransformerException {
        final Action action = action(context.language(), goal);
        final FileObject resource = analysisResult.source;
        try {
            final IStrategoTerm inputTerm =
                transformer.builderInputTerm(analysisResult.result, resource, context.location());
            logger.debug("Transforming analysis result of {} with '{}'", resource, action.name);
            return transformer.transform(context, analysisResult, action.strategy, inputTerm, resource);
        } catch(TransformerException e) {
            throw e;
        } catch(MetaborgException e) {
            throw new TransformerException("Cannot create input term", e);
        }
    }

    @Override public boolean available(ITransformerGoal goal, IContext context) {
        try {
            action(context.language(), goal);
        } catch(TransformerException e) {
            // Exceptions thrown indicate that transformation is not available.
            return false;
        }
        return true;
    }


    private Action action(ILanguageImpl language, ITransformerGoal goal) throws TransformerException {
        if(!(goal instanceof NamedGoal)) {
            final String message = String.format("Goal %s is not a NamedGoal", goal);
            logger.error(message);
            throw new MetaborgRuntimeException(message);
        }
        final MenusFacet facet = language.facets(MenusFacet.class);
        if(facet == null) {
            final String message = String.format("No menus facet found for %s", language);
            logger.error(message);
            throw new TransformerException(message);
        }
        final NamedGoal namedGoal = (NamedGoal) goal;
        final String actionName = namedGoal.name;
        final Action action = facet.action(actionName);
        if(action == null) {
            final String message = String.format("Action %s not found in %s", actionName, language);
            logger.error(message);
            throw new TransformerException(message);
        }
        return action;
    }
}
