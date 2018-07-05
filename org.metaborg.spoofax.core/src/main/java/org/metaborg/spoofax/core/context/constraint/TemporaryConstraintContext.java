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

    public boolean isRoot(String resource) {
        return context.isRoot(resource);
    }

    public boolean isRoot(FileObject resource) {
        return context.isRoot(resource);
    }

    public String resourceKey(String resource) {
        return context.resourceKey(resource);
    }

    public String resourceKey(FileObject resource) {
        return context.resourceKey(resource);
    }

    public FileObject keyResource(String resource) {
        return context.keyResource(resource);
    }

    public void setInitial(InitialResult value) {
        context.setInitial(value);
    }

    public boolean hasInitial() {
        return context.hasInitial();
    }

    public InitialResult getInitial() {
        return context.getInitial();
    }

    public void setFinal(FinalResult value) {
        context.setFinal(value);
    }

    public boolean hasFinal() {
        return context.hasFinal();
    }

    public FinalResult getFinal() {
        return context.getFinal();
    }

    public boolean hasUnit(String key) {
        return context.hasUnit(key);
    }

    public boolean hasUnit(FileObject resource) {
        return context.hasUnit(resource);
    }

    public boolean setUnit(String key, FileResult value) {
        return context.setUnit(key, value);
    }

    public boolean setUnit(FileObject resource, FileResult value) {
        return context.setUnit(resource, value);
    }

    public FileResult getUnit(String key) {
        return context.getUnit(key);
    }

    public FileResult getUnit(FileObject resource) {
        return context.getUnit(resource);
    }

    public boolean remove(String key) {
        return context.remove(key);
    }

    public boolean remove(FileObject resource) {
        return context.remove(resource);
    }

    public Set<Entry<String, FileResult>> entrySet() {
        return context.entrySet();
    }

    public void clear() {
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