package org.metaborg.spoofax.core.context.scopegraph;

import org.metaborg.core.context.ContextIdentifier;
import org.metaborg.core.context.IContextFactory;

import com.google.inject.Inject;
import com.google.inject.Injector;

public class SingleFileScopeGraphContextFactory implements IContextFactory {

    public static final String name = "scopegraph-singlefile";

    private final Injector injector;


    @Inject public SingleFileScopeGraphContextFactory(Injector injector) {
        this.injector = injector;
    }


    @Override public ISingleFileScopeGraphContext create(ContextIdentifier identifier) {
        return new SingleFileScopeGraphContext(injector, identifier);
    }

    @Override public TemporarySingleFileScopeGraphContext createTemporary(ContextIdentifier identifier) {
        return new TemporarySingleFileScopeGraphContext(create(identifier));
    }

}
