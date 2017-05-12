package org.metaborg.spoofax.core.stratego.primitive;

import org.metaborg.core.MetaborgException;
import org.metaborg.core.build.dependency.IDependencyService;
import org.metaborg.core.context.IContext;
import org.metaborg.core.language.ILanguageComponent;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.spoofax.core.stratego.primitive.generic.ASpoofaxContextPrimitive;
import org.spoofax.interpreter.core.Tools;
import org.spoofax.interpreter.stratego.Strategy;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;

import com.google.inject.Inject;

public class IsLanguageActivePrimitive extends ASpoofaxContextPrimitive {
    private final IDependencyService dependencyService;


    @Inject public IsLanguageActivePrimitive(IDependencyService dependencyService) {
        super("is_language_active", 0, 0);

        this.dependencyService = dependencyService;
    }


    @Override protected IStrategoTerm call(IStrategoTerm current, Strategy[] svars, IStrategoTerm[] tvars,
        ITermFactory factory, IContext currentContext) throws MetaborgException {
        final String languageName = Tools.asJavaString(current);

        // GTODO: require language identifier instead of language name
        for(ILanguageComponent component : dependencyService.compileDeps(currentContext.project())) {
            for(ILanguageImpl impl : component.contributesTo()) {
                if(impl != null && impl.belongsTo().name().equals(languageName)) {
                    return current;
                }
            }
        }
        return null;
    }
}
