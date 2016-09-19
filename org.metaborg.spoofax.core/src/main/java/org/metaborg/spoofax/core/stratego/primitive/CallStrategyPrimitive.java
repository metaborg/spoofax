package org.metaborg.spoofax.core.stratego.primitive;

import org.metaborg.core.MetaborgException;
import org.metaborg.core.config.IProjectConfig;
import org.metaborg.core.context.IContext;
import org.metaborg.core.context.IContextService;
import org.metaborg.core.language.ILanguage;
import org.metaborg.core.language.ILanguageComponent;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.language.ILanguageService;
import org.metaborg.core.language.LanguageIdentifier;
import org.metaborg.core.project.IProjectService;
import org.metaborg.spoofax.core.stratego.IStrategoCommon;
import org.metaborg.spoofax.core.stratego.primitive.generic.ASpoofaxContextPrimitive;
import org.spoofax.interpreter.core.Tools;
import org.spoofax.interpreter.stratego.Strategy;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;

import com.google.inject.Inject;

public class CallStrategyPrimitive extends ASpoofaxContextPrimitive {
    private final IContextService contextService;
    private final ILanguageService languageService;

    private final IStrategoCommon common;


    @Inject public CallStrategyPrimitive(IContextService contextService, ILanguageService languageService,
        IProjectService projectService, IStrategoCommon common) {
        super("call_strategy", 0, 2);

        this.contextService = contextService;
        this.languageService = languageService;
        this.common = common;
    }


    @Override protected IStrategoTerm call(IStrategoTerm current, Strategy[] svars, IStrategoTerm[] tvars,
        ITermFactory factory, IContext currentContext) throws MetaborgException {
        final String languageName = Tools.asJavaString(tvars[0]);
        final String strategyName = Tools.asJavaString(tvars[1]);

        // GTODO: require language identifier instead of language name
        IProjectConfig config = currentContext.project().config();
        try {
            if(config != null) {
                for(LanguageIdentifier id : config.compileDeps()) {
                    ILanguageImpl activeImpl = languageService.getImpl(id);
                    if(activeImpl == null || !activeImpl.belongsTo().name().equals(languageName)) {
                        continue;
                    }
                    IContext context = contextService.get(currentContext.location(), currentContext.project(), activeImpl);
                    ILanguageComponent component = languageService.getComponent(id);
                    if(component == null) {
                        continue;
                    }
                    IStrategoTerm result = common.invoke(component, context, current, strategyName);
                    if(result != null) {
                        return result;
                    }
                }
                final String message = String.format("Stratego strategy call of '%s' into language %s failed, no suitable components found.",
                    strategyName, languageName);
                throw new MetaborgException(message);

            } else {
                ILanguage lang = languageService.getLanguage(languageName);
                if(lang == null) {
                    final String message = String.format("Stratego strategy call of '%s' into language %s failed, language not found.",
                        strategyName, languageName);
                    throw new MetaborgException(message);
                }
                ILanguageImpl activeImpl = lang.activeImpl();
                if(activeImpl == null) {
                    final String message = String.format("Stratego strategy call of '%s' into language %s failed, no active implementation found.",
                        strategyName, languageName);
                    throw new MetaborgException(message);
                }
                IContext context = contextService.get(currentContext.location(), currentContext.project(), activeImpl);
                return common.invoke(activeImpl, context, current, strategyName);
            }
        } catch(MetaborgException e) {
            final String message = String.format("Stratego strategy call of '%s' into language %s failed unexpectedly",
                strategyName, languageName);
            throw new MetaborgException(message, e);
        }
    }
}
