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
import org.spoofax.interpreter.terms.IStrategoTerm;

import com.google.inject.assistedinject.Assisted;

public class JavaResolverFacet implements IResolverFacet {
    public final String javaClassName;

    private final IDynamicClassLoadingService semanticProviderService;
    private final ISpoofaxTracingService tracingService;


    @Inject public JavaResolverFacet(IDynamicClassLoadingService semanticProviderService,
        ISpoofaxTracingService tracingService, @Assisted String javaClassName) {
        this.semanticProviderService = semanticProviderService;
        this.tracingService = tracingService;
        this.javaClassName = javaClassName;
    }


    @Override public Resolution resolve(FileObject source, IContext context, Iterable<IStrategoTerm> inRegion,
        ILanguageComponent contributor) throws MetaborgException {
        IResolver resolver = semanticProviderService.loadClass(contributor, javaClassName, IResolver.class);
        Iterable<ResolutionTarget> resolutions = null;
        ISourceLocation highlightLocation = null;
        for(IStrategoTerm region : inRegion) {
            resolutions = resolver.resolve(context, region);
            if(resolutions != null) {
                highlightLocation = tracingService.location(region);
                break;
            }
        }
        if(resolutions == null) {
            return null;
        }
        return new Resolution(highlightLocation.region(), resolutions);
    }
}
