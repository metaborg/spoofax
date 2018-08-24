package org.metaborg.spoofax.core.outline;

import java.util.function.Function;

import org.metaborg.core.MetaborgException;
import org.metaborg.core.context.IContext;
import org.metaborg.core.language.ILanguageComponent;
import org.metaborg.core.outline.IOutlineNode;
import org.metaborg.core.source.ISourceRegion;
import org.metaborg.spoofax.core.semantic_provider.IBuilderInput;
import org.metaborg.spoofax.core.semantic_provider.ISemanticProviderService;
import org.metaborg.spoofax.core.user_definable.IOutliner;
import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.LoggerUtils;
import org.spoofax.interpreter.terms.IStrategoTerm;

import com.google.common.collect.Iterators;

public class JavaOutliner implements IOutliner {
    private static final ILogger logger = LoggerUtils.logger(JavaOutliner.class);
    private final ISemanticProviderService semanticProviderService;
    private final ILanguageComponent component;

    public JavaOutliner(ISemanticProviderService semanticProviderService, ILanguageComponent component) {
        this.semanticProviderService = semanticProviderService;
        this.component = component;
    }

    @Override
    public Iterable<IOutlineNode> createOutline(IContext env, IBuilderInput input,
            Function<IStrategoTerm, ISourceRegion> region) {
        Iterable<IGeneratedOutliner> outliners = () -> {
            try {
                return semanticProviderService.loadClasses(component, IGeneratedOutliner.class);
            } catch (MetaborgException e) {
                logger.warn("Outlining using generated Java classes didn't work: {}", e.getMessage());
                return Iterators.emptyIterator();
            }
        };
        for (IGeneratedOutliner outliner : outliners) {
            Iterable<IOutlineNode> outline = outliner.createOutline(env, input, region);
            if(outline != null) {
                return outline;
            }
        }
        return null;
    }
}
