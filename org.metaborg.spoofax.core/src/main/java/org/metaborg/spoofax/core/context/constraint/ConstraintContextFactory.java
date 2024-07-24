package org.metaborg.spoofax.core.context.constraint;

import jakarta.inject.Inject;
import org.metaborg.core.context.ContextIdentifier;
import org.metaborg.core.context.IContextFactory;

import com.google.inject.Injector;

public class ConstraintContextFactory implements IContextFactory {

    public static final String name = "constraint";

    private final Injector injector;

    @Inject public ConstraintContextFactory(Injector injector) {
        this.injector = injector;
    }

    @Override public IConstraintContext create(ContextIdentifier identifier) {
        return new ConstraintContext(injector, identifier);
    }

    @Override public TemporaryConstraintContext createTemporary(ContextIdentifier identifier) {
        return new TemporaryConstraintContext(create(identifier));
    }

}