package org.metaborg.spoofax.core.context;

import java.io.IOException;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.context.ContextIdentifier;
import org.metaborg.core.context.IContext;
import org.metaborg.core.context.IContextInternal;
import org.metaborg.core.context.ITemporaryContextInternal;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.util.concurrent.IClosableLock;
import org.metaborg.util.concurrent.NullClosableLock;

import com.google.inject.Injector;

public class LegacyContext implements IContext, IContextInternal, ITemporaryContextInternal {
    private final Injector injector;
    private final ContextIdentifier identifier;


    public LegacyContext(Injector injector, ContextIdentifier identifier) {
        this.injector = injector;
        this.identifier = identifier;
    }


    @Override public ContextIdentifier identifier() {
        return identifier;
    }

    @Override public FileObject location() {
        return identifier.location;
    }

    @Override public ILanguageImpl language() {
        return identifier.language;
    }

    @Override public Injector injector() {
        return injector;
    }


    @Override public IClosableLock read() {
        return new NullClosableLock();
    }

    @Override public IClosableLock write() {
        return new NullClosableLock();
    }


    @Override public void persist() throws IOException {
    }

    @Override public void reset() throws IOException {
    }

    @Override public void unload() {
    }


    @Override public void init() {
    }

    @Override public void load() {
    }


    @Override public void close() {
    }
}
