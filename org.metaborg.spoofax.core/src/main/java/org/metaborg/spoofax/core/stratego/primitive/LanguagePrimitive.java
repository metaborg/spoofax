package org.metaborg.spoofax.core.stratego.primitive;

import java.io.IOException;

import org.metaborg.core.MetaborgException;
import org.metaborg.core.context.IContext;
import org.metaborg.core.language.ILanguage;
import org.metaborg.spoofax.core.stratego.primitive.generic.ASpoofaxContextPrimitive;
import org.spoofax.interpreter.stratego.Strategy;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;


public class LanguagePrimitive extends ASpoofaxContextPrimitive {
    @jakarta.inject.Inject public LanguagePrimitive() {
        super("language", 0, 0);
    }


    @Override protected IStrategoTerm call(IStrategoTerm current, Strategy[] svars, IStrategoTerm[] tvars,
        ITermFactory factory, IContext context) throws MetaborgException, IOException {
        final ILanguage lang = context.language().belongsTo();
        return factory.makeString(lang.name());
    }
}
