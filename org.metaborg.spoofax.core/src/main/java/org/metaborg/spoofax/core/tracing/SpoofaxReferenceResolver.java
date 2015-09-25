package org.metaborg.spoofax.core.tracing;

import java.util.Collection;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.MetaborgException;
import org.metaborg.core.analysis.AnalysisFileResult;
import org.metaborg.core.context.IContext;
import org.metaborg.core.language.FacetContribution;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.source.ISourceLocation;
import org.metaborg.core.source.ISourceRegion;
import org.metaborg.core.source.SourceRegion;
import org.metaborg.core.syntax.ParseResult;
import org.metaborg.core.tracing.IReferenceResolver;
import org.metaborg.core.tracing.Resolution;
import org.metaborg.spoofax.core.stratego.IStrategoRuntimeService;
import org.metaborg.spoofax.core.terms.ITermFactoryService;
import org.metaborg.util.concurrent.IClosableLock;
import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.LoggerUtils;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.strategoxt.HybridInterpreter;

import com.google.common.collect.Lists;
import com.google.inject.Inject;

import fj.P2;

public class SpoofaxReferenceResolver implements IReferenceResolver<IStrategoTerm, IStrategoTerm>,
    ISpoofaxReferenceResolver {
    private static final ILogger logger = LoggerUtils.logger(SpoofaxReferenceResolver.class);

    private final ITermFactoryService termFactoryService;
    private final IStrategoRuntimeService strategoRuntimeService;
    private final ISpoofaxTracingService tracingService;
    private final SpoofaxTracingCommon common;


    @Inject public SpoofaxReferenceResolver(ITermFactoryService termFactoryService,
        IStrategoRuntimeService strategoRuntimeService, ISpoofaxTracingService tracingService,
        SpoofaxTracingCommon common) {
        this.termFactoryService = termFactoryService;
        this.strategoRuntimeService = strategoRuntimeService;
        this.tracingService = tracingService;
        this.common = common;
    }


    @Override public boolean available(ILanguageImpl language) {
        return language.facet(ResolverFacet.class) != null;
    }

    @Override public Resolution resolve(int offset, ParseResult<IStrategoTerm> result) throws MetaborgException {
        if(result.result == null) {
            return null;
        }

        final FileObject resource = result.source;
        final ILanguageImpl language = result.language;

        final FacetContribution<ResolverFacet> facetContrib = facet(language);
        final ResolverFacet facet = facetContrib.facet;
        final String strategy = facet.strategyName;

        try {
            final ITermFactory termFactory = termFactoryService.get(facetContrib.contributor);
            final HybridInterpreter interpreter = strategoRuntimeService.runtime(facetContrib.contributor, resource);
            final Iterable<IStrategoTerm> inRegion = tracingService.toParsed(result, new SourceRegion(offset));
            final P2<IStrategoTerm, ISourceRegion> tuple =
                common.outputs(termFactory, interpreter, result.source, result.source, result.result, inRegion,
                    strategy);
            return resolve(tuple);
        } catch(MetaborgException e) {
            throw new MetaborgException("Reference resolution failed", e);
        }
    }

    @Override public Resolution resolve(int offset, AnalysisFileResult<IStrategoTerm, IStrategoTerm> result)
        throws MetaborgException {
        if(result.result == null) {
            return null;
        }

        final IContext context = result.context;
        final ILanguageImpl language = context.language();

        final FacetContribution<ResolverFacet> facetContrib = facet(language);
        final ResolverFacet facet = facetContrib.facet;
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
            return resolve(tuple);
        } catch(MetaborgException e) {
            throw new MetaborgException("Reference resolution failed", e);
        }
    }


    private FacetContribution<ResolverFacet> facet(ILanguageImpl language) throws MetaborgException {
        final FacetContribution<ResolverFacet> facet = language.facetContribution(ResolverFacet.class);
        if(facet == null) {
            final String message =
                logger.format("Cannot resolve reference of {}, it does not have a resolver facet", language);
            throw new MetaborgException(message);
        }
        return facet;
    }

    private Resolution resolve(@Nullable P2<IStrategoTerm, ISourceRegion> tuple) {
        if(tuple == null) {
            return null;
        }

        final IStrategoTerm output = tuple._1();
        final ISourceRegion offsetRegion = tuple._2();

        final Collection<ISourceLocation> targets = Lists.newLinkedList();
        if(output.getTermType() == IStrategoTerm.LIST) {
            for(IStrategoTerm subterm : output) {
                final ISourceLocation targetLocation = common.getTargetLocation(subterm);
                if(targetLocation == null) {
                    logger.debug("Cannot get target location for {}", subterm);
                    continue;
                }
                targets.add(targetLocation);
            }
        } else {
            final ISourceLocation targetLocation = common.getTargetLocation(output);
            if(targetLocation == null) {
                logger.debug("Reference resolution failed, cannot get target location for {}", output);
                return null;
            }
            targets.add(targetLocation);
        }

        if(targets.isEmpty()) {
            logger.debug("Reference resolution failed, cannot get target locations for {}", output);
            return null;
        }
        return new Resolution(offsetRegion, targets);
    }
}
