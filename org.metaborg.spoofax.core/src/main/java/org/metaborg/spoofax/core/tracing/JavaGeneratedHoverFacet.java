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
import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.LoggerUtils;
import org.spoofax.interpreter.terms.IStrategoTerm;

public class JavaGeneratedHoverFacet implements IHoverFacet {
    private static final ILogger logger = LoggerUtils.logger(JavaGeneratedHoverFacet.class);

    private @Inject IDynamicClassLoadingService semanticProviderService;
    private @Inject ISpoofaxTracingService tracingService;

    @Override public Hover hover(FileObject source, IContext context, ILanguageComponent contributor,
        Iterable<IStrategoTerm> inRegion) throws MetaborgException {
        String hoverText = null;
        ISourceLocation highlightLocation = null;
        try {
            regionloop:
            for (IStrategoTerm region : inRegion) {
                for (IHoverText hoverer : semanticProviderService.loadClasses(contributor, IHoverText.Generated.class)) {
                    hoverText = hoverer.createHoverText(context, region);
                    if(hoverText != null) {
                        highlightLocation = tracingService.location(region);
                        break regionloop;
                    }
                }
            }
            if(hoverText == null) {
                return null;
            }
            return new Hover(highlightLocation.region(), hoverText);
        } catch (MetaborgException e) {
            logger.warn("Outlining using generated Java classes didn't work: {}", e.getMessage());
        }
        return null;
    }

}
