package org.metaborg.spoofax.core.stratego.primitive;

import java.io.IOException;

import org.metaborg.core.MetaborgException;
import org.metaborg.core.context.IContext;
import org.metaborg.core.language.ILanguageComponent;
import org.metaborg.core.language.LanguageIdentifier;
import org.metaborg.spoofax.core.stratego.primitive.generic.ASpoofaxContextPrimitive;
import org.spoofax.interpreter.stratego.Strategy;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.IStrategoTuple;
import org.spoofax.interpreter.terms.ITermFactory;

import com.google.inject.Inject;

public class LanguageComponentsPrimitive extends ASpoofaxContextPrimitive {
    @Inject public LanguageComponentsPrimitive() {
        super("language_components", 0, 0);
    }


    @Override protected IStrategoTerm call(IStrategoTerm current, Strategy[] svars, IStrategoTerm[] tvars,
        ITermFactory factory, IContext context) throws MetaborgException, IOException {
        IStrategoList list = factory.makeList();
        for(ILanguageComponent component : context.language().components()) {
            final LanguageIdentifier id = component.id();
            final IStrategoString groupIdTerm = factory.makeString(id.groupId);
            final IStrategoString idTerm = factory.makeString(id.id);
            final IStrategoString versionTerm = factory.makeString(id.version.toString());
            final IStrategoString locationTerm = factory.makeString(component.location().getName().getURI());
            final IStrategoTuple tuple = factory.makeTuple(groupIdTerm, idTerm, versionTerm, locationTerm);
            list = factory.makeListCons(tuple, list);
        }
        return list;
    }
}
