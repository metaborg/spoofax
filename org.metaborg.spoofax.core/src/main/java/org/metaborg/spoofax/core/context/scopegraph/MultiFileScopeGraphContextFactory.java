package org.metaborg.spoofax.core.context.scopegraph;

import org.metaborg.core.context.ContextIdentifier;
import org.metaborg.core.context.IContextFactory;

import com.google.inject.Inject;
import com.google.inject.Injector;

public class MultiFileScopeGraphContextFactory implements IContextFactory {

    public static final String name = "scopegraph-multifile";

    private final Injector injector;


    @Inject public MultiFileScopeGraphContextFactory(Injector injector) {
        this.injector = injector;
    }


    @Override public IMultiFileScopeGraphContext create(ContextIdentifier identifier) {
        return new MultiFileScopeGraphContext(injector, identifier);
    }

    @Override public TemporaryMultiFileScopeGraphContext createTemporary(
            ContextIdentifier identifier) {
        return new TemporaryMultiFileScopeGraphContext(create(identifier));
    }

}
