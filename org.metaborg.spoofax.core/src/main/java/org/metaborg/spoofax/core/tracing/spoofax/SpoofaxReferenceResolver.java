package org.metaborg.spoofax.core.tracing.spoofax;

import java.io.File;
import java.util.Collection;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.spoofax.core.SpoofaxException;
import org.metaborg.spoofax.core.analysis.AnalysisFileResult;
import org.metaborg.spoofax.core.context.IContext;
import org.metaborg.spoofax.core.language.ILanguage;
import org.metaborg.spoofax.core.messages.ISourceLocation;
import org.metaborg.spoofax.core.messages.SourceRegion;
import org.metaborg.spoofax.core.resource.IResourceService;
import org.metaborg.spoofax.core.stratego.IStrategoRuntimeService;
import org.metaborg.spoofax.core.stratego.StrategoLocalPath;
import org.metaborg.spoofax.core.stratego.StrategoRuntimeUtils;
import org.metaborg.spoofax.core.terms.ITermFactoryService;
import org.metaborg.spoofax.core.tracing.IReferenceResolver;
import org.metaborg.spoofax.core.tracing.Resolution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.strategoxt.HybridInterpreter;

import com.google.common.collect.Lists;
import com.google.inject.Inject;

public class SpoofaxReferenceResolver implements IReferenceResolver<IStrategoTerm, IStrategoTerm>,
    ISpoofaxReferenceResolver {
    private static final Logger logger = LoggerFactory.getLogger(SpoofaxReferenceResolver.class);

    private final IResourceService resourceService;
    private final ITermFactoryService termFactoryService;
    private final IStrategoRuntimeService strategoRuntimeService;
    private final ISpoofaxTracingService tracingService;

    private final StrategoLocalPath localPath;


    @Inject public SpoofaxReferenceResolver(IResourceService resourceService, ITermFactoryService termFactoryService,
        IStrategoRuntimeService strategoRuntimeService, ISpoofaxTracingService tracingService,
        StrategoLocalPath localPath) {
        this.resourceService = resourceService;
        this.termFactoryService = termFactoryService;
        this.strategoRuntimeService = strategoRuntimeService;
        this.tracingService = tracingService;
        this.localPath = localPath;
    }


    @Override public @Nullable Resolution resolve(int offset, AnalysisFileResult<IStrategoTerm, IStrategoTerm> result)
        throws SpoofaxException {
        if(result.result == null) {
            return null;
        }

        final FileObject resource = result.source;
        final IContext context = result.context;
        final ILanguage language = context.language();
        final ITermFactory termFactory = termFactoryService.get(language);

        try {
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
                final IStrategoTerm output = StrategoRuntimeUtils.invoke(runtime, inputTerm, "editor-resolve");
                if(output == null) {
                    continue;
                }

                final ISourceLocation highlightLocation = tracingService.fromAnalyzed(term);
                if(highlightLocation == null) {
                    logger.debug("Reference resolution failed, cannot get source location for {}", term);
                    return null;
                }

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
                return new Resolution(highlightLocation.region(), targets);
            }
        } catch(SpoofaxException e) {
            throw new SpoofaxException("Reference resolution failed", e);
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
