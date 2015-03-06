package org.metaborg.spoofax.core.context;

public class SpoofaxContextFactory implements IContextFactory {
    @Override public IContextInternal create(ContextIdentifier identifier) {
        return new SpoofaxContext(identifier);
    }
}
