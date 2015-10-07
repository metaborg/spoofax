package org.metaborg.spoofax.core.context;

import org.metaborg.core.context.ContextIdentifier;
import org.metaborg.core.context.IContextFactory;
import org.metaborg.core.context.IContextInternal;

import com.google.inject.Inject;
import com.google.inject.Injector;

public class LegacyContextFactory implements IContextFactory {
    public static final String name = "legacy";
    
    private final Injector injector;


    @Inject public LegacyContextFactory(Injector injector) {
        this.injector = injector;
    }


    @Override public IContextInternal create(ContextIdentifier identifier) {
        return new LegacyContext(injector, identifier);
    }
}
