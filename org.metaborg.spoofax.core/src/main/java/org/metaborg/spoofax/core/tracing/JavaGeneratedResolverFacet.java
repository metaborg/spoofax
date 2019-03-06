package org.metaborg.spoofax.core.tracing;

import javax.inject.Inject;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.MetaborgException;
import org.metaborg.core.context.IContext;
import org.metaborg.core.language.ILanguageComponent;
import org.metaborg.core.source.ISourceLocation;
import org.metaborg.core.tracing.Resolution;
import org.metaborg.core.tracing.ResolutionTarget;
import org.metaborg.spoofax.core.dynamicclassloading.IDynamicClassLoadingService;
import org.metaborg.spoofax.core.dynamicclassloading.api.IResolver;
import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.LoggerUtils;
import org.spoofax.interpreter.terms.IStrategoTerm;

public class JavaGeneratedResolverFacet implements IResolverFacet {
    private static final ILogger logger = LoggerUtils.logger(JavaGeneratedResolverFacet.class);

    private @Inject IDynamicClassLoadingService semanticProviderService;
    private @Inject ISpoofaxTracingService tracingService;

    @Override public Resolution resolve(FileObject source, IContext context, Iterable<IStrategoTerm> inRegion,
        ILanguageComponent contributor) throws MetaborgException {
        Iterable<ResolutionTarget> resolutions = null;
        ISourceLocation highlightLocation = null;
        try {
            regionloop:
            for (IStrategoTerm region : inRegion) {
                for (IResolver resolver : semanticProviderService.loadClasses(contributor, IResolver.Generated.class)) {
                    resolutions = resolver.resolve(context, region);
                    if(resolutions != null) {
                        highlightLocation = tracingService.location(region);
                        break regionloop;
                    }
                }
            }
            if(resolutions == null) {
                return null;
            }
            return new Resolution(highlightLocation.region(), resolutions);
        } catch (MetaborgException e) {
            logger.warn("Outlining using generated Java classes didn't work: {}", e.getMessage());
        }
        return null;
    }

}
