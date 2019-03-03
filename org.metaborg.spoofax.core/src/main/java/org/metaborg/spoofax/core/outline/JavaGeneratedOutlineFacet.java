package org.metaborg.spoofax.core.outline;

import javax.inject.Inject;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.MetaborgException;
import org.metaborg.core.context.IContext;
import org.metaborg.core.language.ILanguageComponent;
import org.metaborg.core.outline.IOutline;
import org.metaborg.core.outline.IOutlineNode;
import org.metaborg.core.outline.Outline;
import org.metaborg.spoofax.core.dynamicclassloading.IBuilderInput;
import org.metaborg.spoofax.core.dynamicclassloading.IDynamicClassLoadingService;
import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.LoggerUtils;

public class JavaGeneratedOutlineFacet implements IOutlineFacet {
    private static final ILogger logger = LoggerUtils.logger(JavaGeneratedOutlineFacet.class);

    private final int expandTo;
    private @Inject IDynamicClassLoadingService semanticProviderService;

    public JavaGeneratedOutlineFacet(int expandTo) {
        this.expandTo = expandTo;
    }

    @Override public int getExpansionLevel() {
        return expandTo;
    }

    @Override public IOutline createOutline(FileObject source, IContext context, ILanguageComponent contributor,
        IBuilderInput input) throws MetaborgException {
        try {
            for (IGeneratedOutliner outliner : semanticProviderService.loadClasses(contributor, IGeneratedOutliner.class)) {
                Iterable<IOutlineNode> outline = outliner.createOutline(context, input);
                if(outline != null) {
                    return new Outline(outline, getExpansionLevel());
                }
            }
        } catch (MetaborgException e) {
            logger.warn("Outlining using generated Java classes didn't work: {}", e.getMessage());
        }
        return null;
    }

}
