package org.metaborg.spoofax.core.terms;

import org.metaborg.core.language.ILanguageComponent;
import org.metaborg.core.language.ILanguageImpl;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.jsglr.client.imploder.ImploderOriginTermFactory;
import org.spoofax.terms.TermFactory;

public class TermFactoryService implements ITermFactoryService {
    private final ITermFactory genericFactory = new ImploderOriginTermFactory(new TermFactory());


    @Override public ITermFactory get(ILanguageImpl impl) {
        return genericFactory;
    }

    @Override public ITermFactory get(ILanguageComponent component) {
        return genericFactory;
    }

    @Override public ITermFactory getGeneric() {
        return genericFactory;
    }
}
