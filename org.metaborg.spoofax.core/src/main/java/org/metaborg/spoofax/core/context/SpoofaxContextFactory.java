package org.metaborg.spoofax.core.context;

import org.metaborg.core.context.ContextIdentifier;
import org.metaborg.core.context.IContextFactory;
import org.metaborg.core.context.IContextInternal;
import org.metaborg.core.resource.IResourceService;

import com.google.inject.Inject;
import com.google.inject.Injector;

public class SpoofaxContextFactory implements IContextFactory {
    private final Injector injector;
    private final IResourceService resourceService;


    @Inject public SpoofaxContextFactory(Injector injector, IResourceService resourceService) {
        this.injector = injector;
        this.resourceService = resourceService;
    }


    @Override public IContextInternal create(ContextIdentifier identifier) {
        return new SpoofaxContext(resourceService, identifier, injector);
    }
}
