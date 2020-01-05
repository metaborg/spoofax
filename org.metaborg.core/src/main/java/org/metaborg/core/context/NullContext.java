package org.metaborg.core.context;

import java.io.IOException;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.project.IProject;
import org.metaborg.util.concurrent.IClosableLock;
import org.metaborg.util.concurrent.NullClosableLock;

import com.google.inject.Injector;

class NullContext implements IContext, ITemporaryContext, IContextInternal {

    private final ContextIdentifier identifier;
    private final FileObject location;
    private final IProject project;
    private final ILanguageImpl language;
    private final Injector injector;

    public NullContext(FileObject location, IProject project, ILanguageImpl language, Injector injector) {
        this.identifier = new ContextIdentifier(location, project, language);
        this.location = location;
        this.project = project;
        this.language = language;
        this.injector = injector;
    }

    // IContext

    @Override public FileObject location() {
        return location;
    }

    @Override public IProject project() {
        return project;
    }

    @Override public ILanguageImpl language() {
        return language;
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

    // ITemporaryContext

    @Override public void close() {
    }

    // IContextInternal

    @Override public ContextIdentifier identifier() {
        return identifier;
    }

    @Override public void init() {
    }

    @Override public void load() {
    }

    @Override public void unload() {
    }

}