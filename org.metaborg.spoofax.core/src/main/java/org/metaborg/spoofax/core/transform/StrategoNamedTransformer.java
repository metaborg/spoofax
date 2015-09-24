package org.metaborg.spoofax.core.transform;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.MetaborgException;
import org.metaborg.core.MetaborgRuntimeException;
import org.metaborg.core.analysis.AnalysisFileResult;
import org.metaborg.core.context.IContext;
import org.metaborg.core.language.ILanguageComponent;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.menu.ActionContribution;
import org.metaborg.core.menu.IAction;
import org.metaborg.core.menu.IMenuService;
import org.metaborg.core.syntax.ParseResult;
import org.metaborg.core.transform.ITransformerGoal;
import org.metaborg.core.transform.NamedGoal;
import org.metaborg.core.transform.NestedNamedGoal;
import org.metaborg.core.transform.TransformResult;
import org.metaborg.core.transform.TransformerException;
import org.metaborg.spoofax.core.menu.MenuFacet;
import org.metaborg.spoofax.core.menu.StrategoTransformAction;
import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.LoggerUtils;
import org.spoofax.interpreter.terms.IStrategoTerm;

import com.google.inject.Inject;

import fj.P;
import fj.P2;

/**
 * Transformer executor for the {@link NamedGoal} and {@link MenuFacet}.
 */
public class StrategoNamedTransformer implements IStrategoTransformerExecutor {
    private static final ILogger logger = LoggerUtils.logger(StrategoNamedTransformer.class);

    private final IMenuService menuService;

    private final StrategoTransformerCommon transformer;


    @Inject public StrategoNamedTransformer(StrategoTransformerCommon transformer, IMenuService menuService) {
        this.transformer = transformer;
        this.menuService = menuService;
    }


    @Override public TransformResult<ParseResult<IStrategoTerm>, IStrategoTerm> transform(
        ParseResult<IStrategoTerm> parseResult, IContext context, ITransformerGoal goal) throws TransformerException {
        final P2<StrategoTransformAction, ILanguageComponent> tuple = action(context.language(), goal);
        final StrategoTransformAction action = tuple._1();
        final ILanguageComponent component = tuple._2();

        final FileObject resource = parseResult.source;
        try {
            final IStrategoTerm inputTerm =
                transformer.builderInputTerm(parseResult.result, resource, context.location());
            logger.debug("Transforming parse result of {} with '{}'", resource, action.name);
            return transformer.transform(component, context, parseResult, action.strategy, inputTerm, resource);
        } catch(TransformerException e) {
            throw e;
        } catch(MetaborgException e) {
            throw new TransformerException("Cannot create input term", e);
        }
    }

    @Override public TransformResult<AnalysisFileResult<IStrategoTerm, IStrategoTerm>, IStrategoTerm> transform(
        AnalysisFileResult<IStrategoTerm, IStrategoTerm> analysisResult, IContext context, ITransformerGoal goal)
        throws TransformerException {
        final FileObject resource = analysisResult.source;
        if(analysisResult.result == null) {
            final String message = logger.format("Cannot transform {}, analyzed AST is null", resource);
            throw new TransformerException(message);
        }
        final P2<StrategoTransformAction, ILanguageComponent> tuple = action(context.language(), goal);
        final StrategoTransformAction action = tuple._1();
        final ILanguageComponent component = tuple._2();

        try {
            final IStrategoTerm inputTerm;
            try {
                inputTerm = transformer.builderInputTerm(analysisResult.result, resource, context.location());
            } catch(MetaborgException e) {
                throw new TransformerException("Cannot create input term", e);
            }
            logger.debug("Transforming analysis result of {} with '{}'", resource, action.name);
            return transformer.transform(component, context, analysisResult, action.strategy, inputTerm, resource);
        } catch(TransformerException e) {
            throw e;
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


    private P2<StrategoTransformAction, ILanguageComponent> action(ILanguageImpl language, ITransformerGoal goal)
        throws TransformerException {
        final ActionContribution actionContrib;
        if(goal instanceof NamedGoal) {
            final NamedGoal namedGoal = (NamedGoal) goal;
            try {
                actionContrib = menuService.actionContribution(language, namedGoal.name);
            } catch(MetaborgException e) {
                throw new TransformerException(e);
            }
        } else if(goal instanceof NestedNamedGoal) {
            final NestedNamedGoal namedGoal = (NestedNamedGoal) goal;
            try {
                actionContrib = menuService.nestedActionContribution(language, namedGoal.names);
            } catch(MetaborgException e) {
                throw new TransformerException(e);
            }
        } else {
            final String message = String.format("Goal %s of class %s is not a named goal", goal, goal.getClass());
            throw new MetaborgRuntimeException(message);
        }

        if(actionContrib == null) {
            final String message = String.format("Action %s not found in %s", goal, language);
            throw new TransformerException(message);
        }

        final IAction action = actionContrib.action;
        if(!(action instanceof StrategoTransformAction)) {
            final String message = String.format("Action %s is not a Stratego transformation action", goal);
            throw new TransformerException(message);
        }

        return P.p((StrategoTransformAction) action, actionContrib.contributor);
    }
}
