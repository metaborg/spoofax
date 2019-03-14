package org.metaborg.spoofax.core.tracing;

import javax.inject.Inject;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.MetaborgException;
import org.metaborg.core.context.IContext;
import org.metaborg.core.language.ILanguageComponent;
import org.metaborg.core.source.ISourceLocation;
import org.metaborg.core.tracing.Hover;
import org.metaborg.spoofax.core.dynamicclassloading.IDynamicClassLoadingService;
import org.metaborg.spoofax.core.dynamicclassloading.api.IHoverText;
import org.spoofax.interpreter.terms.IStrategoTerm;

import com.google.inject.assistedinject.Assisted;

public class JavaHoverFacet implements IHoverFacet {
    public final String javaClassName;

    private final IDynamicClassLoadingService semanticProviderService;
    private final ISpoofaxTracingService tracingService;

    @Inject public JavaHoverFacet(IDynamicClassLoadingService semanticProviderService,
        ISpoofaxTracingService tracingService, @Assisted String javaClassName) {
        this.semanticProviderService = semanticProviderService;
        this.tracingService = tracingService;
        this.javaClassName = javaClassName;
    }


    @Override public Hover hover(FileObject source, IContext context, ILanguageComponent contributor,
        Iterable<IStrategoTerm> inRegion) throws MetaborgException {
        IHoverText hoverer = semanticProviderService.loadClass(contributor, javaClassName, IHoverText.class);
        String hoverText = null;
        ISourceLocation highlightLocation = null;
        for (IStrategoTerm region : inRegion) {
            hoverText = hoverer.createHoverText(context, region);
            if(hoverText != null) {
                highlightLocation = tracingService.location(region);
                break;
            }
        }
        if(hoverText == null) {
            return null;
        }
        return new Hover(highlightLocation.region(), hoverText);
    }
}
