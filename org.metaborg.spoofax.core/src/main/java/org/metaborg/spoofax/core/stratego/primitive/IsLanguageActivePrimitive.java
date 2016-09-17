package org.metaborg.spoofax.core.stratego.primitive;

import org.metaborg.core.MetaborgException;
import org.metaborg.core.config.IProjectConfig;
import org.metaborg.core.context.IContext;
import org.metaborg.core.language.ILanguage;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.language.ILanguageService;
import org.metaborg.core.language.LanguageIdentifier;
import org.metaborg.spoofax.core.stratego.primitive.generic.ASpoofaxContextPrimitive;
import org.spoofax.interpreter.core.Tools;
import org.spoofax.interpreter.stratego.Strategy;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;

import com.google.inject.Inject;

public class IsLanguageActivePrimitive extends ASpoofaxContextPrimitive {
    private final ILanguageService languageService;


    @Inject public IsLanguageActivePrimitive(ILanguageService languageService) {
        super("is_language_active", 0, 0);

        this.languageService = languageService;
    }


    @Override protected IStrategoTerm call(IStrategoTerm current, Strategy[] svars, IStrategoTerm[] tvars,
        ITermFactory factory, IContext currentContext) throws MetaborgException {
        final String languageName = Tools.asJavaString(current);

        // GTODO: require language identifier instead of language name
        IProjectConfig config = currentContext.project().config();
        if(config != null) {
            for(LanguageIdentifier id : config.compileDeps()) {
                ILanguageImpl impl = languageService.getImpl(id);
                if(impl != null && impl.belongsTo().name().equals(languageName)) {
                    return current;
                }
            }
            return null;
        } else {
            ILanguage lang = languageService.getLanguage(languageName);
            if(lang == null) {
                return null;
            }
            ILanguageImpl impl = lang.activeImpl();
            if(impl == null) {
                return null;
            }
            return current;
        }
    }
}
