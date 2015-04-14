package org.metaborg.spoofax.core.transform.stratego;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.metaborg.spoofax.core.analysis.AnalysisFileResult;
import org.metaborg.spoofax.core.context.IContext;
import org.metaborg.spoofax.core.language.ILanguage;
import org.metaborg.spoofax.core.messages.IMessage;
import org.metaborg.spoofax.core.stratego.IStrategoRuntimeService;
import org.metaborg.spoofax.core.stratego.StrategoRuntimeUtils;
import org.metaborg.spoofax.core.syntax.ParseResult;
import org.metaborg.spoofax.core.terms.ITermFactoryService;
import org.metaborg.spoofax.core.transform.ITransformer;
import org.metaborg.spoofax.core.transform.TransformResult;
import org.metaborg.spoofax.core.transform.TransformerException;
import org.metaborg.util.iterators.Iterables2;
import org.metaborg.util.time.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.strategoxt.HybridInterpreter;

import com.google.inject.Inject;

public class StrategoTransformer implements ITransformer<IStrategoTerm, IStrategoTerm, IStrategoTerm> {
    private static final Logger logger = LoggerFactory.getLogger(StrategoTransformer.class);

    private final IStrategoRuntimeService strategoRuntimeService;
    private final ITermFactoryService termFactoryService;


    @Inject public StrategoTransformer(IStrategoRuntimeService strategoRuntimeService,
        ITermFactoryService termFactoryService) {
        this.strategoRuntimeService = strategoRuntimeService;
        this.termFactoryService = termFactoryService;
    }


    @Override public TransformResult<ParseResult<IStrategoTerm>, IStrategoTerm> transformParsed(
        ParseResult<IStrategoTerm> parseResult, IContext context, String transformer) {
        final MenusFacet facet = context.language().facet(MenusFacet.class);
        checkFacet(facet, context.language(), transformer);
        final Action action = facet.action(transformer);

        final FileObject resource = parseResult.source;
        final ITermFactory termFactory = termFactoryService.getGeneric();
        final IStrategoTerm inputTerm = inputTerm(termFactory, parseResult.result, resource, context.location());
        final HybridInterpreter runtime = strategoRuntimeService.runtime(context);
        final Timer timer = new Timer(true);
        final IStrategoTerm result = StrategoRuntimeUtils.invoke(runtime, inputTerm, action.strategy);
        final long duration = timer.stop();

        final TransformResult<ParseResult<IStrategoTerm>, IStrategoTerm> transResult =
            new TransformResult<ParseResult<IStrategoTerm>, IStrategoTerm>(result, Iterables2.<IMessage>empty(),
                Iterables2.singleton(resource), context.language(), duration, parseResult);
        return transResult;
    }

    @Override public TransformResult<AnalysisFileResult<IStrategoTerm, IStrategoTerm>, IStrategoTerm>
        transformAnalyzed(AnalysisFileResult<IStrategoTerm, IStrategoTerm> analysisResult, IContext context,
            String transformer) {
        final MenusFacet facet = context.language().facet(MenusFacet.class);
        checkFacet(facet, context.language(), transformer);
        final Action action = facet.action(transformer);

        final FileObject resource = analysisResult.file();
        final ITermFactory termFactory = termFactoryService.getGeneric();
        final IStrategoTerm inputTerm = inputTerm(termFactory, analysisResult.result(), resource, context.location());
        final HybridInterpreter runtime = strategoRuntimeService.runtime(context);
        final Timer timer = new Timer(true);
        final IStrategoTerm result = StrategoRuntimeUtils.invoke(runtime, inputTerm, action.strategy);
        final long duration = timer.stop();

        final TransformResult<AnalysisFileResult<IStrategoTerm, IStrategoTerm>, IStrategoTerm> transResult =
            new TransformResult<AnalysisFileResult<IStrategoTerm, IStrategoTerm>, IStrategoTerm>(result,
                Iterables2.<IMessage>empty(), Iterables2.singleton(resource), context.language(), duration,
                analysisResult);
        return transResult;
    }

    private void checkFacet(MenusFacet facet, ILanguage language, String transformer) {
        if(facet == null) {
            final String message =
                String.format("No menus facet found for %s, cannot perform transformation", language);
            logger.error(message);
            throw new TransformerException(message);
        }

        if(facet.action(transformer) == null) {
            final String message =
                String.format("Transformer %s not found in %s, cannot perform transformation", transformer, language);
            logger.error(message);
            throw new TransformerException(message);
        }
    }

    private IStrategoTerm inputTerm(ITermFactory termFactory, IStrategoTerm ast, FileObject resource,
        FileObject location) {
        // GTODO: support selected node
        final IStrategoTerm node = ast;
        // GTODO: support position
        final IStrategoTerm position = termFactory.makeList();
        final IStrategoTerm locationTerm = termFactory.makeString(location.getName().getPath());
        String path;
        try {
            path = location.getName().getRelativeName(resource.getName());
        } catch(FileSystemException e) {
            path = resource.getName().getPath();
        }
        return termFactory.makeTuple(node, position, ast, termFactory.makeString(path), locationTerm);
    }
}
