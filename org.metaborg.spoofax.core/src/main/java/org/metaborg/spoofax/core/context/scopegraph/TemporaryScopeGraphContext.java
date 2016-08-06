package org.metaborg.spoofax.core.context.scopegraph;

import java.io.IOException;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.context.ContextIdentifier;
import org.metaborg.core.context.ITemporaryContextInternal;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.project.IProject;
import org.metaborg.util.concurrent.IClosableLock;
import org.metaborg.util.concurrent.NullClosableLock;

import com.google.inject.Injector;

public class TemporaryScopeGraphContext implements IScopeGraphContext, ITemporaryContextInternal {

    private final ScopeGraphContext context;
    
    public TemporaryScopeGraphContext(ScopeGraphContext context) {
        this.context = context;
    }


    @Override
    public ContextIdentifier identifier() {
        return context.identifier();
    }

    @Override
    public void init() {
        context.init();
    }

    @Override
    public void load() {
        // Temporary context is not persisted, no loading needed.
    }

    @Override
    public void unload() {
        context.unload();
    }

    @Override
    public FileObject location() {
        return context.location();
    }

    @Override
    public IProject project() {
        return context.project();
    }

    @Override
    public ILanguageImpl language() {
        return context.language();
    }

    @Override
    public Injector injector() {
        return context.injector();
    }

    @Override
    public IClosableLock read() {
        return new NullClosableLock();
    }

    @Override
    public IClosableLock write() {
        return new NullClosableLock();
    }

    @Override
    public void persist() throws IOException {
        // Temporary context is not persisted.
    }

    @Override
    public void reset() throws IOException {
        context.reset();
    }

    @Override
    public void close() {
        unload();
    }


    @Override
    public void addUnit(IScopeGraphUnit unit) {
        context.addUnit(unit);
    }

    @Override
    public IScopeGraphUnit unit(String source) {
        return context.unit(source);
    }

    @Override
    public Iterable<IScopeGraphUnit> units() {
        return context.units();
    }

    @Override
    public void removeUnit(String source) {
        context.removeUnit(source);
    }

}
