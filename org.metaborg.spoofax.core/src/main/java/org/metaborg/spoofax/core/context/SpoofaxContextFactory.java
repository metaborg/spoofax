package org.metaborg.spoofax.core.context;

import org.metaborg.core.context.ContextIdentifier;
import org.metaborg.core.context.IContextFactory;
import org.metaborg.core.context.IContextInternal;
import org.metaborg.spoofax.core.terms.ITermFactoryService;

import com.google.inject.Inject;
import com.google.inject.Injector;

public class SpoofaxContextFactory implements IContextFactory {
    private final Injector injector;
    private final ITermFactoryService termFactoryService;


    @Inject public SpoofaxContextFactory(Injector injector, ITermFactoryService termFactoryService) {
        this.injector = injector;
        this.termFactoryService = termFactoryService;
    }


    @Override public IContextInternal create(ContextIdentifier identifier) {
        return new SpoofaxContext(injector, termFactoryService, identifier);
    }
}
