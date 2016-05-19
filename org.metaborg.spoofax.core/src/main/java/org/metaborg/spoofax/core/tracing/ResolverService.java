package org.metaborg.spoofax.core.tracing;

import java.util.Collection;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.MetaborgException;
import org.metaborg.core.context.IContext;
import org.metaborg.core.language.FacetContribution;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.source.ISourceLocation;
import org.metaborg.core.source.ISourceRegion;
import org.metaborg.core.source.SourceRegion;
import org.metaborg.core.tracing.Resolution;
import org.metaborg.spoofax.core.stratego.IStrategoRuntimeService;
import org.metaborg.spoofax.core.terms.ITermFactoryService;
import org.metaborg.spoofax.core.tracing.TracingCommon.TermWithRegion;
import org.metaborg.spoofax.core.unit.ISpoofaxAnalyzeUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxParseUnit;
import org.metaborg.util.concurrent.IClosableLock;
import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.LoggerUtils;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.strategoxt.HybridInterpreter;

import com.google.common.collect.Lists;
import com.google.inject.Inject;

public class ResolverService implements ISpoofaxResolverService {
    private static final ILogger logger = LoggerUtils.logger(ResolverService.class);

    private final ITermFactoryService termFactoryService;
    private final IStrategoRuntimeService strategoRuntimeService;
    private final ISpoofaxTracingService tracingService;
    private final TracingCommon common;


    @Inject public ResolverService(ITermFactoryService termFactoryService,
        IStrategoRuntimeService strategoRuntimeService, ISpoofaxTracingService tracingService, TracingCommon common) {
        this.termFactoryService = termFactoryService;
        this.strategoRuntimeService = strategoRuntimeService;
        this.tracingService = tracingService;
        this.common = common;
    }


    @Override public boolean available(ILanguageImpl language) {
        return language.facet(ResolverFacet.class) != null;
    }

    @Override public Resolution resolve(int offset, ISpoofaxParseUnit result) throws MetaborgException {
        if(!result.valid()) {
            return null;
        }

        final FileObject source = result.source();
        final ILanguageImpl langImpl = result.input().langImpl();

        final FacetContribution<ResolverFacet> facetContrib = facet(langImpl);
        final ResolverFacet facet = facetContrib.facet;
        final String strategy = facet.strategyName;

        try {
            final ITermFactory termFactory = termFactoryService.get(facetContrib.contributor);
            final HybridInterpreter interpreter = strategoRuntimeService.runtime(facetContrib.contributor, source);
            final Iterable<IStrategoTerm> inRegion = tracingService.fragments(result, new SourceRegion(offset));
            final TermWithRegion tuple =
                common.outputs(termFactory, interpreter, source, source, result.ast(), inRegion, strategy);
            return resolve(tuple);
        } catch(MetaborgException e) {
            throw new MetaborgException("Reference resolution failed", e);
        }
    }

    @Override public Resolution resolve(int offset, ISpoofaxAnalyzeUnit result) throws MetaborgException {
        if(!result.valid() || !result.hasAst()) {
            return null;
        }

        final FileObject source = result.source();
        final IContext context = result.context();
        final ILanguageImpl language = context.language();

        final FacetContribution<ResolverFacet> facetContrib = facet(language);
        final ResolverFacet facet = facetContrib.facet;
        final String strategy = facet.strategyName;

        try {
            final ITermFactory termFactory = termFactoryService.get(facetContrib.contributor);
            final HybridInterpreter interpreter = strategoRuntimeService.runtime(facetContrib.contributor, context);
            final Iterable<IStrategoTerm> inRegion = tracingService.fragments(result, new SourceRegion(offset));
            final TermWithRegion tuple;
            try(IClosableLock lock = context.read()) {
                tuple = common.outputs(termFactory, interpreter, source, source, result.ast(), inRegion, strategy);
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

    private Resolution resolve(@Nullable TermWithRegion tuple) {
        if(tuple == null) {
            return null;
        }

        final IStrategoTerm output = tuple.term;
        final ISourceRegion offsetRegion = tuple.region;

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
