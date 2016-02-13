package org.metaborg.spoofax.core.stratego.primitives;

import java.io.File;

import org.metaborg.core.language.ILanguageComponent;
import org.metaborg.core.language.LanguageIdentifier;
import org.metaborg.core.resource.IResourceService;
import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.core.InterpreterException;
import org.spoofax.interpreter.library.AbstractPrimitive;
import org.spoofax.interpreter.stratego.Strategy;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.IStrategoTuple;
import org.spoofax.interpreter.terms.ITermFactory;

import com.google.inject.Inject;

public class ComponentsPrimitive extends AbstractPrimitive {
    private final IResourceService resourceService;


    @Inject public ComponentsPrimitive(IResourceService resourceService) {
        super("language_components", 0, 0);

        this.resourceService = resourceService;
    }


    @Override public boolean call(IContext env, Strategy[] strategies, IStrategoTerm[] terms)
        throws InterpreterException {
        final ITermFactory factory = env.getFactory();
        final org.metaborg.core.context.IContext context = (org.metaborg.core.context.IContext) env.contextObject();

        IStrategoList list = factory.makeList();
        for(ILanguageComponent component : context.language().components()) {
            final LanguageIdentifier id = component.id();
            final IStrategoString groupIdTerm = factory.makeString(id.groupId);
            final IStrategoString idTerm = factory.makeString(id.id);
            final IStrategoString versionTerm = factory.makeString(id.version.toString());
            final File localFile = resourceService.localFile(component.location());
            final IStrategoString locationTerm = factory.makeString(localFile.toString());
            final IStrategoTuple tuple = factory.makeTuple(groupIdTerm, idTerm, versionTerm, locationTerm);
            list = factory.makeListCons(tuple, list);
        }
        env.setCurrent(list);
        return true;
    }
}
