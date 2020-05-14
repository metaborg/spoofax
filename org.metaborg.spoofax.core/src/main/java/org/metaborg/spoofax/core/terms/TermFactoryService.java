package org.metaborg.spoofax.core.terms;

import javax.annotation.Nullable;

import org.metaborg.core.language.ILanguageComponent;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.project.IProject;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.jsglr.client.imploder.ImploderOriginTermFactory;
import org.spoofax.terms.TermFactory;

import com.google.inject.Inject;

/**
 * The reason for this to be a service was to provide typesmart enhanced term factories. Typesmart was dropped, so this
 * service is now basically a stub.
 */
public class TermFactoryService implements ITermFactoryService {
    @Inject public TermFactoryService() {
    }

    private final ITermFactory genericFactory = new ImploderOriginTermFactory(new TermFactory());

    @Override public ITermFactory get(ILanguageImpl impl, @Nullable IProject project) {
        return genericFactory;
    }

    @Override public ITermFactory get(ILanguageComponent component, @Nullable IProject project) {
        return genericFactory;
    }

    @Override public ITermFactory getGeneric() {
        return genericFactory;
    }
}
