package org.metaborg.spoofax.core.context;

import org.metaborg.core.context.ContextIdentifier;
import org.metaborg.core.context.IContextFactory;
import org.spoofax.interpreter.terms.ITermFactory;

import com.google.inject.Inject;
import com.google.inject.Injector;

public class IndexTaskContextFactory implements IContextFactory {
    public static final String name = "index-task";

    private final Injector injector;
    private final ITermFactory termFactory;


    @Inject public IndexTaskContextFactory(Injector injector, ITermFactory termFactory) {
        this.injector = injector;
        this.termFactory = termFactory;
    }


    @Override public IndexTaskContext create(ContextIdentifier identifier) {
        return new IndexTaskContext(injector, termFactory, identifier);
    }

    @Override public IndexTaskTemporaryContext createTemporary(ContextIdentifier identifier) {
        return new IndexTaskTemporaryContext(create(identifier));
    }
}
