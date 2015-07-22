package org.metaborg.spoofax.core.terms;

import org.metaborg.core.language.ILanguageImpl;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.terms.TermFactory;

public class TermFactoryService implements ITermFactoryService {
    private final ITermFactory genericFactory = new TermFactory();


    @Override public ITermFactory get(ILanguageImpl language) {
        // TODO: create language-specific term factory.
        return genericFactory;
    }

    @Override public ITermFactory getGeneric() {
        return genericFactory;
    }
}
