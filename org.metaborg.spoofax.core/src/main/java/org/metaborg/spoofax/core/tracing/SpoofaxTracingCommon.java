package org.metaborg.spoofax.core.tracing;

import java.io.File;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.MetaborgException;
import org.metaborg.core.resource.IResourceService;
import org.metaborg.core.source.ISourceLocation;
import org.metaborg.core.source.ISourceRegion;
import org.metaborg.spoofax.core.stratego.IStrategoCommon;
import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.LoggerUtils;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.strategoxt.HybridInterpreter;

import com.google.inject.Inject;

import fj.P;
import fj.P2;

public class SpoofaxTracingCommon {
    private static final ILogger logger = LoggerUtils.logger(SpoofaxTracingCommon.class);

    private final IResourceService resourceService;
    private final ISpoofaxTracingService tracingService;

    private final IStrategoCommon common;


    @Inject public SpoofaxTracingCommon(IResourceService resourceService, ISpoofaxTracingService tracingService,
        IStrategoCommon common) {
        this.resourceService = resourceService;
        this.tracingService = tracingService;
        this.common = common;
    }


    public P2<IStrategoTerm, ISourceRegion> outputs(ITermFactory termFactory, HybridInterpreter runtime,
        FileObject location, FileObject resource, IStrategoTerm result, Iterable<IStrategoTerm> inRegion,
        String strategy) throws MetaborgException {
        final File localContextLocation = resourceService.localFile(location);
        final File localResource = resourceService.localPath(resource);
        if(localContextLocation == null || localResource == null) {
            return null;
        }
        final IStrategoString path = common.localResourceTerm(localResource, localContextLocation);
        final IStrategoString contextPath = common.localLocationTerm(localContextLocation);

        for(IStrategoTerm term : inRegion) {
            final IStrategoTerm inputTerm =
                termFactory.makeTuple(term, termFactory.makeTuple(), result, path, contextPath);
            final IStrategoTerm output = common.invoke(runtime, inputTerm, strategy);
            if(output == null) {
                continue;
            }

            final ISourceLocation highlightLocation = tracingService.fromAnalyzed(term);
            if(highlightLocation == null) {
                logger.debug("Cannot get source region for {}", term);
                continue;
            }

            return P.p(output, highlightLocation.region());
        }

        return null;
    }

    public @Nullable ISourceLocation getTargetLocation(IStrategoTerm term) {
        final ISourceLocation targetLocation = tracingService.fromAnalyzed(term);
        if(targetLocation == null || targetLocation.resource() == null) {
            return null;
        }
        return targetLocation;
    }
}
