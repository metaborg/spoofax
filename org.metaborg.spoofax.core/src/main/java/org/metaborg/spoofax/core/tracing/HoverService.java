package org.metaborg.spoofax.core.tracing;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.MetaborgException;
import org.metaborg.core.MetaborgRuntimeException;
import org.metaborg.core.context.ContextException;
import org.metaborg.core.context.IContext;
import org.metaborg.core.context.IContextService;
import org.metaborg.core.language.FacetContribution;
import org.metaborg.core.language.ILanguageComponent;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.project.IProject;
import org.metaborg.core.project.IProjectService;
import org.metaborg.core.source.ISourceRegion;
import org.metaborg.core.source.SourceRegion;
import org.metaborg.core.tracing.Hover;
import org.metaborg.spoofax.core.stratego.IStrategoRuntimeService;
import org.metaborg.spoofax.core.terms.ITermFactoryService;
import org.metaborg.spoofax.core.tracing.TracingCommon.TermWithRegion;
import org.metaborg.spoofax.core.unit.ISpoofaxAnalyzeUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxParseUnit;
import org.metaborg.util.concurrent.IClosableLock;
import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.LoggerUtils;
import org.spoofax.interpreter.core.Tools;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.strategoxt.HybridInterpreter;

import com.google.inject.Inject;

public class HoverService implements ISpoofaxHoverService {
    private static final ILogger logger = LoggerUtils.logger(HoverService.class);

    private final IProjectService projectService;
    private final IContextService contextService;
    private final ITermFactoryService termFactoryService;
    private final IStrategoRuntimeService strategoRuntimeService;
    private final ISpoofaxTracingService tracingService;
    private final TracingCommon common;


    @Inject public HoverService(IProjectService projectService, IContextService contextService,
        ITermFactoryService termFactoryService, IStrategoRuntimeService strategoRuntimeService,
        ISpoofaxTracingService tracingService, TracingCommon common) {
        this.projectService = projectService;
        this.contextService = contextService;
        this.termFactoryService = termFactoryService;
        this.strategoRuntimeService = strategoRuntimeService;
        this.tracingService = tracingService;
        this.common = common;
    }


    @Override public boolean available(ILanguageImpl language) {
        return language.facet(HoverFacet.class) != null;
    }

    @Override public Hover hover(int offset, ISpoofaxParseUnit result) throws MetaborgException {
        if(!result.valid()) {
            return null;
        }

        final FileObject source = result.source();
        final IProject project = projectService.get(source);
        final ILanguageImpl langImpl = result.input().langImpl();
        @Nullable IContext context;
        if(project == null) {
            context = null;
        } else {
            try {
                context = contextService.get(source, project, langImpl);
            } catch(ContextException | MetaborgRuntimeException e) {
                // Failed to get a context, ignore and use the source file to get a stratego runtime later.
                context = null;
            }
        }

        final FacetContribution<HoverFacet> facetContrib = facet(langImpl);
        final HoverFacet facet = facetContrib.facet;
        final ILanguageComponent contributor = facetContrib.contributor;
        final String strategy = facet.strategyName;

        try {
            final ITermFactory termFactory = termFactoryService.get(contributor, project, true);
            final HybridInterpreter interpreter;
            if(context == null) {
                interpreter = strategoRuntimeService.runtime(contributor, source, true);
            } else {
                interpreter = strategoRuntimeService.runtime(contributor, context, true);
            }
            final Iterable<IStrategoTerm> inRegion = tracingService.fragments(result, new SourceRegion(offset));
            final TermWithRegion tuple =
                common.outputs(termFactory, interpreter, context.location(), source, result.ast(), inRegion, strategy);
            return hover(tuple);
        } catch(MetaborgException e) {
            throw new MetaborgException("Getting hover tooltip information failed unexpectedly", e);
        }
    }

    @Override public Hover hover(int offset, ISpoofaxAnalyzeUnit result) throws MetaborgException {
        if(!result.valid() || !result.hasAst()) {
            return null;
        }

        final FileObject source = result.source();
        final IContext context = result.context();
        final ILanguageImpl language = context.language();

        final FacetContribution<HoverFacet> facetContrib = facet(language);
        final HoverFacet facet = facetContrib.facet;
        final String strategy = facet.strategyName;

        try {
            final IProject project = context.project();
            final ITermFactory termFactory = termFactoryService.get(facetContrib.contributor, project, true);
            final HybridInterpreter interpreter =
                strategoRuntimeService.runtime(facetContrib.contributor, context, true);
            final Iterable<IStrategoTerm> inRegion = tracingService.fragments(result, new SourceRegion(offset));
            final TermWithRegion tuple;
            try(IClosableLock lock = context.read()) {
                tuple = common.outputs(termFactory, interpreter, context.location(), source, result.ast(), inRegion, strategy);
            }
            return hover(tuple);
        } catch(MetaborgException e) {
            throw new MetaborgException("Getting hover tooltip information failed unexpectedly", e);
        }
    }


    private FacetContribution<HoverFacet> facet(ILanguageImpl language) throws MetaborgException {
        final FacetContribution<HoverFacet> facet = language.facetContribution(HoverFacet.class);
        if(facet == null) {
            final String message =
                logger.format("Cannot get hover tooltip information for {}, it does not have a hover facet", language);
            throw new MetaborgException(message);
        }
        return facet;
    }

    private Hover hover(@Nullable TermWithRegion tuple) {
        if(tuple == null) {
            return null;
        }

        final IStrategoTerm output = tuple.term;
        final ISourceRegion offsetRegion = tuple.region;

        final String text;
        if(output.getTermType() == IStrategoTerm.STRING) {
            text = Tools.asJavaString(output);
        } else {
            text = output.toString();
        }
        final String massagedText = text.replace("\\\"", "\"").replace("\\n", "");

        return new Hover(offsetRegion, massagedText);
    }
}
