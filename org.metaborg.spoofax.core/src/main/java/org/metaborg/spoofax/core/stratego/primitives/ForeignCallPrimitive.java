package org.metaborg.spoofax.core.stratego.primitives;

import org.metaborg.core.MetaborgException;
import org.metaborg.core.context.ContextException;
import org.metaborg.core.context.IContextService;
import org.metaborg.core.language.ILanguage;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.language.ILanguageService;
import org.metaborg.spoofax.core.stratego.IStrategoCommon;
import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.core.InterpreterException;
import org.spoofax.interpreter.core.Tools;
import org.spoofax.interpreter.library.AbstractPrimitive;
import org.spoofax.interpreter.stratego.Strategy;
import org.spoofax.interpreter.terms.IStrategoTerm;

import com.google.inject.Inject;

public class ForeignCallPrimitive extends AbstractPrimitive {
    private final ILanguageService languageService;
    private final IContextService contextService;

    private final IStrategoCommon common;


    @Inject public ForeignCallPrimitive(ILanguageService languageService, IContextService contextService,
        IStrategoCommon common) {
        super("SSL_EXT_foreigncall", 0, 2);

        this.languageService = languageService;
        this.contextService = contextService;
        this.common = common;
    }

    @Override public boolean call(IContext env, Strategy[] strategies, IStrategoTerm[] terms)
        throws InterpreterException {
        final String languageName = Tools.asJavaString(terms[0]);
        final String strategyName = Tools.asJavaString(terms[1]);

        // GTODO: require language identifier instead of language name
        final ILanguage language = languageService.getLanguage(languageName);
        if(language == null) {
            final String message =
                String.format("Foreign call of '%s' into language %s failed, language could not be found",
                    strategyName, languageName);
            throw new InterpreterException(message);
        }
        final ILanguageImpl activeImpl = language.activeImpl();
        if(activeImpl == null) {
            final String message =
                String.format(
                    "Foreign call of '%s' into language %s failed, no active language implementation could be found",
                    strategyName, languageName);
            throw new InterpreterException(message);
        }

        try {
            final org.metaborg.core.context.IContext currentContext =
                (org.metaborg.core.context.IContext) env.contextObject();
            final org.metaborg.core.context.IContext context = contextService.get(currentContext, activeImpl);
            final IStrategoTerm output = common.invoke(activeImpl, context, env.current(), strategyName);
            if(output == null) {
                return false;
            }
            env.setCurrent(output);
            return true;
        } catch(ContextException e) {
            final String message =
                String.format("Foreign call of '%s' into language %s failed", strategyName, languageName);
            throw new InterpreterException(message, e);
        } catch(MetaborgException e) {
            final String message =
                String.format("Foreign call of '%s' into language %s failed", strategyName, languageName);
            throw new InterpreterException(message, e);
        }
    }
}
