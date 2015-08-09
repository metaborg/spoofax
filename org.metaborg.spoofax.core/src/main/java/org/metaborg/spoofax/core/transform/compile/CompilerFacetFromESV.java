package org.metaborg.spoofax.core.transform.compile;

import java.util.List;

import javax.annotation.Nullable;

import org.metaborg.core.language.LanguageIdentifier;
import org.metaborg.spoofax.core.esv.ESVReader;
import org.metaborg.spoofax.core.menu.MenusFacetFromESV;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spoofax.interpreter.core.Tools;
import org.spoofax.interpreter.terms.IStrategoAppl;

public class CompilerFacetFromESV {
    private static final Logger logger = LoggerFactory.getLogger(MenusFacetFromESV.class);


    public static @Nullable CompilerFacet create(IStrategoAppl esv, LanguageIdentifier languageId) {
        final List<IStrategoAppl> onSaveHandlers = ESVReader.collectTerms(esv, "OnSave");
        if(onSaveHandlers.isEmpty()) {
            return null;
        } else if(onSaveHandlers.size() > 1) {
            logger.warn(
                "Found multiple on-save handlers for {}, this is not supported, using the first on-save handler",
                languageId);
        }
        final IStrategoAppl onSaveHandler = onSaveHandlers.get(0);
        final String strategyName = Tools.asJavaString(onSaveHandler.getSubterm(0).getSubterm(0));
        return new CompilerFacet(strategyName);
    }
}
