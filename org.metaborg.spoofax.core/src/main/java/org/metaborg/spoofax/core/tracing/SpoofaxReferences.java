package org.metaborg.spoofax.core.tracing;

import java.io.File;
import java.util.Collection;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.MetaborgException;
import org.metaborg.core.analysis.AnalysisFileResult;
import org.metaborg.core.context.IContext;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.resource.IResourceService;
import org.metaborg.core.source.ISourceLocation;
import org.metaborg.core.source.ISourceRegion;
import org.metaborg.core.source.SourceRegion;
import org.metaborg.core.tracing.Hover;
import org.metaborg.core.tracing.IHoverService;
import org.metaborg.core.tracing.IReferenceResolver;
import org.metaborg.core.tracing.Resolution;
import org.metaborg.spoofax.core.stratego.IStrategoRuntimeService;
import org.metaborg.spoofax.core.stratego.StrategoFacet;
import org.metaborg.spoofax.core.stratego.StrategoLocalPath;
import org.metaborg.spoofax.core.stratego.StrategoRuntimeUtils;
import org.metaborg.spoofax.core.terms.ITermFactoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spoofax.interpreter.core.Tools;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.strategoxt.HybridInterpreter;

import com.google.common.collect.Lists;
import com.google.inject.Inject;

import fj.P;
import fj.P2;

public class SpoofaxReferences implements IReferenceResolver<IStrategoTerm, IStrategoTerm>,
    IHoverService<IStrategoTerm, IStrategoTerm>, ISpoofaxReferenceResolver, ISpoofaxHoverService {
    private static final Logger logger = LoggerFactory.getLogger(SpoofaxReferences.class);

    private final IResourceService resourceService;
    private final ITermFactoryService termFactoryService;
    private final IStrategoRuntimeService strategoRuntimeService;
    private final ISpoofaxTracingService tracingService;

    private final StrategoLocalPath localPath;


    @Inject public SpoofaxReferences(IResourceService resourceService, ITermFactoryService termFactoryService,
        IStrategoRuntimeService strategoRuntimeService, ISpoofaxTracingService tracingService,
        StrategoLocalPath localPath) {
        this.resourceService = resourceService;
        this.termFactoryService = termFactoryService;
        this.strategoRuntimeService = strategoRuntimeService;
        this.tracingService = tracingService;
        this.localPath = localPath;
    }


    @Override public @Nullable Resolution resolve(int offset, AnalysisFileResult<IStrategoTerm, IStrategoTerm> result)
        throws MetaborgException {
        final ILanguageImpl language = result.context.language();
        final StrategoFacet facet = language.facets(StrategoFacet.class);
        if(facet == null) {
            return null;
        }
        final String resolverStrategy = facet.resolverStrategy();
        if(resolverStrategy == null) {
            return null;
        }

        try {
            final P2<IStrategoTerm, ISourceRegion> tuple = outputs(offset, result, resolverStrategy);
            if(tuple == null) {
                return null;
            }

            final IStrategoTerm output = tuple._1();
            final ISourceRegion offsetRegion = tuple._2();

            final Collection<ISourceLocation> targets = Lists.newLinkedList();
            if(output.getTermType() == IStrategoTerm.LIST) {
                for(IStrategoTerm subterm : output) {
                    final ISourceLocation targetLocation = getTargetLocation(subterm);
                    if(targetLocation == null) {
                        logger.debug("Cannot get target location for {}", subterm);
                        continue;
                    }
                    targets.add(targetLocation);
                }
            } else {
                final ISourceLocation targetLocation = getTargetLocation(output);
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
        } catch(MetaborgException e) {
            throw new MetaborgException("Reference resolution failed", e);
        }
    }

    @Override public Hover hover(int offset, AnalysisFileResult<IStrategoTerm, IStrategoTerm> result)
        throws MetaborgException {
        final ILanguageImpl language = result.context.language();
        final StrategoFacet facet = language.facets(StrategoFacet.class);
        if(facet == null) {
            return null;
        }
        final String hoverStrategy = facet.hoverStrategy();
        if(hoverStrategy == null) {
            return null;
        }

        try {
            final P2<IStrategoTerm, ISourceRegion> tuple = outputs(offset, result, hoverStrategy);
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
        } catch(MetaborgException e) {
            throw new MetaborgException("Hover information creation failed unexpectedly", e);
        }
    }

    private P2<IStrategoTerm, ISourceRegion> outputs(int offset,
        AnalysisFileResult<IStrategoTerm, IStrategoTerm> result, String strategy) throws MetaborgException {
        if(result.result == null) {
            return null;
        }

        final FileObject resource = result.source;
        final IContext context = result.context;
        final ILanguageImpl language = context.language();
        final ITermFactory termFactory = termFactoryService.get(language);

        final HybridInterpreter runtime = strategoRuntimeService.runtime(context);

        final File localContextLocation = resourceService.localFile(context.location());
        final File localResource = resourceService.localPath(resource);
        if(localContextLocation == null || localResource == null) {
            return null;
        }
        final IStrategoString path = localPath.localResourceTerm(localResource, localContextLocation);
        final IStrategoString contextPath = localPath.localLocationTerm(localContextLocation);

        final Iterable<IStrategoTerm> inRegion = tracingService.toAnalyzed(result, new SourceRegion(offset));
        for(IStrategoTerm term : inRegion) {
            final IStrategoTerm inputTerm =
                termFactory.makeTuple(term, termFactory.makeTuple(), result.result, path, contextPath);
            final IStrategoTerm output = StrategoRuntimeUtils.invoke(runtime, inputTerm, strategy);
            if(output == null) {
                continue;
            }

            final ISourceLocation highlightLocation = tracingService.fromAnalyzed(term);
            if(highlightLocation == null) {
                logger.debug("Reference resolution failed, cannot get source region for {}", term);
                continue;
            }

            return P.p(output, highlightLocation.region());
        }

        return null;
    }

    private @Nullable ISourceLocation getTargetLocation(IStrategoTerm term) {
        final ISourceLocation targetLocation = tracingService.fromAnalyzed(term);
        if(targetLocation == null || targetLocation.resource() == null) {
            return null;
        }
        return targetLocation;
    }
}
