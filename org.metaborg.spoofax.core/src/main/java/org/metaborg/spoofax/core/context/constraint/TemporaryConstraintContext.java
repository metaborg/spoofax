package org.metaborg.spoofax.core.context.constraint;

import java.io.IOException;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.context.ContextIdentifier;
import org.metaborg.core.context.ITemporaryContextInternal;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.project.IProject;
import org.metaborg.util.concurrent.IClosableLock;
import org.metaborg.util.concurrent.NullClosableLock;
import org.spoofax.interpreter.terms.IStrategoTerm;

import com.google.inject.Injector;

public class TemporaryConstraintContext implements IConstraintContext, ITemporaryContextInternal {

    private final IConstraintContext context;

    public TemporaryConstraintContext(IConstraintContext context) {
        this.context = context;
        init();
    }

    // -------------------------------------

    public Mode mode() {
        return context.mode();
    }

    public boolean put(String resource, IStrategoTerm value) {
        return context.put(resource, value);
    }

    public boolean remove(String resource) {
        return context.remove(resource);
    }

    public Set<Entry<String, IStrategoTerm>> entrySet() {
        return context.entrySet();
    }

    public void clear() {
        context.clear();
    }

    // -------------------------------------

    @Override public ContextIdentifier identifier() {
        return context.identifier();
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

    @Override public FileObject location() {
        return context.location();
    }

    @Override public IProject project() {
        return context.project();
    }

    @Override public ILanguageImpl language() {
        return context.language();
    }

    @Override public Injector injector() {
        return context.injector();
    }

    @Override public IClosableLock read() {
        return new NullClosableLock();
    }

    @Override public IClosableLock write() {
        return new NullClosableLock();
    }

    @Override public void persist() throws IOException {
        // Temporary context is not persisted.
    }

    @Override public void reset() throws IOException {
        context.reset();
    }

    @Override public void close() {
        unload();
    }

}