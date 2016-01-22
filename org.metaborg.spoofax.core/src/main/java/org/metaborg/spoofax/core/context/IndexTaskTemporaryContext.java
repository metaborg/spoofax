package org.metaborg.spoofax.core.context;

import java.io.IOException;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.context.ContextIdentifier;
import org.metaborg.core.context.ITemporaryContextInternal;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.runtime.task.engine.ITaskEngine;
import org.metaborg.util.concurrent.IClosableLock;
import org.metaborg.util.concurrent.NullClosableLock;
import org.spoofax.interpreter.library.index.IIndex;

import com.google.inject.Injector;

public class IndexTaskTemporaryContext implements ITemporaryContextInternal, IIndexTaskContext {
    private final IndexTaskContext context;


    public IndexTaskTemporaryContext(IndexTaskContext context) {
        this.context = context;
        init();
    }

    
    @Override public ContextIdentifier identifier() {
        return context.identifier();
    }

    @Override public FileObject location() {
        return context.location();
    }

    @Override public ILanguageImpl language() {
        return context.language();
    }

    @Override public Injector injector() {
        return context.injector();
    }

    @Override public IIndex index() {
        return context.index();
    }

    @Override public ITaskEngine taskEngine() {
        return context.taskEngine();
    }


    @Override public IClosableLock read() {
        // Temporary context is not thread-safe, no locking needed.
        return new NullClosableLock();
    }

    @Override public IClosableLock write() {
        // Temporary context is not thread-safe, no locking needed.
        return new NullClosableLock();
    }


    @Override public void persist() throws IOException {
        // Temporary context is not persisted.
    }

    @Override public void reset() throws IOException {
        // Temporary context is not persisted, no cleaning needed.
    }


    @Override public void init() {
        context.init();
    }

    @Override public void load() {
        // Temporary context is not persisted, no loading needed.
    }

    @Override public void unload() {
        context.unload();
    }


    @Override public void close() {
        unload();
    }
}
