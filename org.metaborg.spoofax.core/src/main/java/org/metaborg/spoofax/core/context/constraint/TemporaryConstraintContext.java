package org.metaborg.spoofax.core.context.constraint;

import java.io.IOException;
import java.util.List;
import java.util.Map;
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

    @Override public String resourceKey(FileObject resource) {
        return context.resourceKey(resource);
    }

    @Override public FileObject keyResource(String resource) {
        return context.keyResource(resource);
    }

    @Override public boolean contains(FileObject resource) {
        return context.contains(resource);
    }

    @Override public boolean put(FileObject resource, int contentHash, IStrategoTerm analyzedAst, IStrategoTerm value,
            IStrategoTerm errors, IStrategoTerm warnings, IStrategoTerm notes, List<String> exceptions) {
        return context.put(resource, contentHash, analyzedAst, value, errors, warnings, notes, exceptions);
    }

    @Override public Entry get(FileObject resource) {
        return context.get(resource);
    }

    @Override public boolean remove(FileObject resource) {
        return context.remove(resource);
    }

    @Override public Set<Map.Entry<String, Entry>> entrySet() {
        return context.entrySet();
    }

    @Override public void clear() {
        context.clear();
    }

    // -------------------------------------

    @Override public ContextIdentifier identifier() {
        return context.identifier();
    }

    @Override public int hashCode() {
        return context.hashCode();
    }

    @Override public boolean equals(Object other) {
        return context.equals(other);
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