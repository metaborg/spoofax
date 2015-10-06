package org.metaborg.spoofax.core.tracing;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.MetaborgException;
import org.metaborg.core.analysis.AnalysisFileResult;
import org.metaborg.core.context.IContext;
import org.metaborg.core.language.FacetContribution;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.source.ISourceRegion;
import org.metaborg.core.source.SourceRegion;
import org.metaborg.core.syntax.ParseResult;
import org.metaborg.core.tracing.Hover;
import org.metaborg.core.tracing.IHoverService;
import org.metaborg.spoofax.core.stratego.IStrategoRuntimeService;
import org.metaborg.spoofax.core.terms.ITermFactoryService;
import org.metaborg.util.concurrent.IClosableLock;
import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.LoggerUtils;
import org.spoofax.interpreter.core.Tools;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.strategoxt.HybridInterpreter;

import com.google.inject.Inject;

import fj.P2;

public class HoverService implements IHoverService<IStrategoTerm, IStrategoTerm>, ISpoofaxHoverService {
    private static final ILogger logger = LoggerUtils.logger(HoverService.class);

    private final ITermFactoryService termFactoryService;
    private final IStrategoRuntimeService strategoRuntimeService;
    private final ISpoofaxTracingService tracingService;
    private final TracingCommon common;


    @Inject public HoverService(ITermFactoryService termFactoryService,
        IStrategoRuntimeService strategoRuntimeService, ISpoofaxTracingService tracingService,
        TracingCommon common) {
        this.termFactoryService = termFactoryService;
        this.strategoRuntimeService = strategoRuntimeService;
        this.tracingService = tracingService;
        this.common = common;
    }


    @Override public boolean available(ILanguageImpl language) {
        return language.facet(HoverFacet.class) != null;
    }

    @Override public Hover hover(int offset, ParseResult<IStrategoTerm> result) throws MetaborgException {
        if(result.result == null) {
            return null;
        }

        final FileObject resource = result.source;
        final ILanguageImpl language = result.language;

        final FacetContribution<HoverFacet> facetContrib = facet(language);
        final HoverFacet facet = facetContrib.facet;
        final String strategy = facet.strategyName;

        try {
            final ITermFactory termFactory = termFactoryService.get(facetContrib.contributor);
            final HybridInterpreter interpreter = strategoRuntimeService.runtime(facetContrib.contributor, resource);
            final Iterable<IStrategoTerm> inRegion = tracingService.toParsed(result, new SourceRegion(offset));
            final P2<IStrategoTerm, ISourceRegion> tuple =
                common.outputs(termFactory, interpreter, result.source, result.source, result.result, inRegion,
                    strategy);
            return hover(tuple);
        } catch(MetaborgException e) {
            throw new MetaborgException("Getting hover tooltip information failed unexpectedly", e);
        }
    }

    @Override public Hover hover(int offset, AnalysisFileResult<IStrategoTerm, IStrategoTerm> result)
        throws MetaborgException {
        if(result.result == null) {
            return null;
        }

        final IContext context = result.context;
        final ILanguageImpl language = context.language();

        final FacetContribution<HoverFacet> facetContrib = facet(language);
        final HoverFacet facet = facetContrib.facet;
        final String strategy = facet.strategyName;

        try {
            final ITermFactory termFactory = termFactoryService.get(facetContrib.contributor);
            final HybridInterpreter interpreter = strategoRuntimeService.runtime(facetContrib.contributor, context);
            final Iterable<IStrategoTerm> inRegion = tracingService.toAnalyzed(result, new SourceRegion(offset));
            final P2<IStrategoTerm, ISourceRegion> tuple;
            try(IClosableLock lock = context.read()) {
                tuple =
                    common.outputs(termFactory, interpreter, result.source, result.source, result.result, inRegion,
                        strategy);
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

    private Hover hover(@Nullable P2<IStrategoTerm, ISourceRegion> tuple) {
        if(tuple == null) {
            return null;
        }

        final IStrategoTerm output = tuple._1();
        final ISourceRegion offsetRegion = tuple._2();

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
