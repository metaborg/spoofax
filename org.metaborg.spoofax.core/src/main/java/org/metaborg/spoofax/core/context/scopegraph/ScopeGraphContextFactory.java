package org.metaborg.spoofax.core.context.scopegraph;

import org.metaborg.core.context.ContextIdentifier;
import org.metaborg.core.context.IContextFactory;

import com.google.inject.Inject;
import com.google.inject.Injector;

public class ScopeGraphContextFactory implements IContextFactory {
    public static final String name = "scopegraph";

    private final Injector injector;


    @Inject public ScopeGraphContextFactory(Injector injector) {
        this.injector = injector;
    }


    @Override public ScopeGraphContext create(ContextIdentifier identifier) {
        return new ScopeGraphContext(injector, identifier);
    }

    @Override public TemporaryScopeGraphContext createTemporary(ContextIdentifier identifier) {
        return new TemporaryScopeGraphContext(new ScopeGraphContext(injector, identifier));
    }

}
