package org.metaborg.spoofax.core.stratego.primitive.legacy;

import org.metaborg.core.MetaborgException;
import org.metaborg.core.context.IContext;
import org.metaborg.core.context.IContextService;
import org.metaborg.core.language.ILanguage;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.language.ILanguageService;
import org.metaborg.core.project.IProject;
import org.metaborg.core.project.IProjectService;
import org.metaborg.spoofax.core.stratego.IStrategoCommon;
import org.metaborg.spoofax.core.stratego.primitive.generic.ASpoofaxContextPrimitive;
import org.spoofax.interpreter.core.Tools;
import org.spoofax.interpreter.stratego.Strategy;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;

import com.google.inject.Inject;

public class LegacyForeignCallPrimitive extends ASpoofaxContextPrimitive {
    private final ILanguageService languageService;
    private final IContextService contextService;
    private final IProjectService projectService;

    private final IStrategoCommon common;


    @Inject public LegacyForeignCallPrimitive(ILanguageService languageService, IContextService contextService,
        IProjectService projectService, IStrategoCommon common) {
        super("SSL_EXT_foreigncall", 0, 2);

        this.languageService = languageService;
        this.contextService = contextService;
        this.projectService = projectService;
        this.common = common;
    }


    @Override protected IStrategoTerm call(IStrategoTerm current, Strategy[] svars, IStrategoTerm[] tvars,
        ITermFactory factory, IContext currentContext) throws MetaborgException {
        final String languageName = Tools.asJavaString(tvars[0]);
        final String strategyName = Tools.asJavaString(tvars[1]);

        // GTODO: require language identifier instead of language name
        final ILanguage language = languageService.getLanguage(languageName);
        if(language == null) {
            final String message =
                String.format("Stratego strategy call of '%s' into language %s failed, language could not be found",
                    strategyName, languageName);
            throw new MetaborgException(message);
        }
        final ILanguageImpl activeImpl = language.activeImpl();
        if(activeImpl == null) {
            final String message = String.format(
                "Stratego strategy call of '%s' into language %s failed, no active language implementation could be found",
                strategyName, languageName);
            throw new MetaborgException(message);
        }

        try {
            final IProject project = projectService.get(currentContext.location());
            IContext context = contextService.get(currentContext.location(), project, activeImpl);
            return common.invoke(activeImpl, context, current, strategyName);
        } catch(MetaborgException e) {
            final String message = String.format("Stratego strategy call of '%s' into language %s failed unexpectedly",
                strategyName, languageName);
            throw new MetaborgException(message, e);
        }
    }
}
