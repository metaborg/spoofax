package org.metaborg.spoofax.core.context;

import org.metaborg.core.context.ContextIdentifier;
import org.metaborg.core.context.IContextFactory;
import org.metaborg.spoofax.core.terms.ITermFactoryService;

import com.google.inject.Inject;
import com.google.inject.Injector;

public class IndexTaskContextFactory implements IContextFactory {
    public static final String name = "index-task";

    private final Injector injector;
    private final ITermFactoryService termFactoryService;


    @Inject public IndexTaskContextFactory(Injector injector, ITermFactoryService termFactoryService) {
        this.injector = injector;
        this.termFactoryService = termFactoryService;
    }


    @Override public IndexTaskContext create(ContextIdentifier identifier) {
        return new IndexTaskContext(injector, termFactoryService, identifier);
    }

    @Override public IndexTaskTemporaryContext createTemporary(ContextIdentifier identifier) {
        return new IndexTaskTemporaryContext(create(identifier));
    }
}
