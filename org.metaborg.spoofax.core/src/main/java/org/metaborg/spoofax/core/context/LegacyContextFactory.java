package org.metaborg.spoofax.core.context;

import org.metaborg.core.context.ContextIdentifier;
import org.metaborg.core.context.IContextFactory;

import com.google.inject.Injector;

public class LegacyContextFactory implements IContextFactory {
    public static final String name = "legacy";

    private final Injector injector;


    @jakarta.inject.Inject public LegacyContextFactory(Injector injector) {
        this.injector = injector;
    }


    @Override public LegacyContext create(ContextIdentifier identifier) {
        return new LegacyContext(injector, identifier);
    }

    @Override public LegacyContext createTemporary(ContextIdentifier identifier) {
        return create(identifier);
    }
}
