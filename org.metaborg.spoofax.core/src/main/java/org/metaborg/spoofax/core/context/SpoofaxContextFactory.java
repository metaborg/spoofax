package org.metaborg.spoofax.core.context;

import org.metaborg.core.context.ContextIdentifier;
import org.metaborg.core.context.IContextFactory;
import org.metaborg.core.context.IContextInternal;
import org.metaborg.core.resource.IResourceService;

import com.google.inject.Inject;

public class SpoofaxContextFactory implements IContextFactory {
    private final IResourceService resourceService;


    @Inject public SpoofaxContextFactory(IResourceService resourceService) {
        this.resourceService = resourceService;
    }


    @Override public IContextInternal create(ContextIdentifier identifier) {
        return new SpoofaxContext(resourceService, identifier);
    }
}
