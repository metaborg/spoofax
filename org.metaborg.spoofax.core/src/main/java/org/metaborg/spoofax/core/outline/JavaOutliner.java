package org.metaborg.spoofax.core.outline;

import java.util.function.Function;

import org.metaborg.core.MetaborgException;
import org.metaborg.core.context.IContext;
import org.metaborg.core.language.ILanguageComponent;
import org.metaborg.core.outline.IOutlineNode;
import org.metaborg.core.source.ISourceRegion;
import org.metaborg.spoofax.core.dynamicclassloading.IBuilderInput;
import org.metaborg.spoofax.core.dynamicclassloading.IDynamicClassLoadingService;
import org.metaborg.spoofax.core.dynamicclassloading.api.IOutliner;
import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.LoggerUtils;
import org.spoofax.interpreter.terms.IStrategoTerm;

public class JavaOutliner implements IOutliner {
    private static final ILogger logger = LoggerUtils.logger(JavaOutliner.class);
    private final IDynamicClassLoadingService semanticProviderService;
    private final ILanguageComponent component;

    public JavaOutliner(IDynamicClassLoadingService semanticProviderService, ILanguageComponent component) {
        this.semanticProviderService = semanticProviderService;
        this.component = component;
    }

    @Override
    public Iterable<IOutlineNode> createOutline(IContext env, IBuilderInput input,
            Function<IStrategoTerm, ISourceRegion> region) {
        try {
            for (IGeneratedOutliner outliner : semanticProviderService.loadClasses(component, IGeneratedOutliner.class)) {
                Iterable<IOutlineNode> outline = outliner.createOutline(env, input, region);
                if(outline != null) {
                    return outline;
                }
            }
        } catch (MetaborgException e) {
            logger.warn("Outlining using generated Java classes didn't work: {}", e.getMessage());
        }
        return null;
    }
}
