package org.metaborg.spoofax.core.stratego.primitive;

import java.io.IOException;

import org.metaborg.core.MetaborgException;
import org.metaborg.core.context.IContext;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.language.LanguageIdentifier;
import org.metaborg.spoofax.core.stratego.primitive.generic.ASpoofaxContextPrimitive;
import org.spoofax.interpreter.stratego.Strategy;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;


public class LanguageImplementationPrimitive extends ASpoofaxContextPrimitive {
    @jakarta.inject.Inject @javax.inject.Inject public LanguageImplementationPrimitive() {
        super("language_implementation", 0, 0);
    }


    @Override protected IStrategoTerm call(IStrategoTerm current, Strategy[] svars, IStrategoTerm[] tvars,
        ITermFactory factory, IContext context) throws MetaborgException, IOException {
        final ILanguageImpl langImpl = context.language();
        final LanguageIdentifier langId = langImpl.id();
        final IStrategoString groupIdTerm = factory.makeString(langId.groupId);
        final IStrategoString idTerm = factory.makeString(langId.id);
        final IStrategoString versionTerm = factory.makeString(langId.version.toString());
        return factory.makeTuple(groupIdTerm, idTerm, versionTerm);
    }
}
