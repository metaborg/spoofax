package org.metaborg.spoofax.core.context;

import org.metaborg.core.context.ContextIdentifier;
import org.metaborg.core.context.IContextFactory;
import org.metaborg.spoofax.core.context.scopegraph.ScopeGraphContext;

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

    @Override public ScopeGraphContext createTemporary(ContextIdentifier identifier) {
        return create(identifier);
    }

}
