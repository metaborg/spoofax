package org.metaborg.spoofax.core.context.scopegraph;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.metaborg.core.build.CommonPaths;
import org.metaborg.core.context.ContextIdentifier;
import org.metaborg.core.context.IContextInternal;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.project.IProject;
import org.metaborg.util.concurrent.ClosableLock;
import org.metaborg.util.concurrent.IClosableLock;
import org.metaborg.util.file.FileUtils;
import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.LoggerUtils;

import com.google.inject.Injector;

abstract class AbstractScopeGraphContext<S extends Serializable> implements IContextInternal {

    private static final ILogger logger = LoggerUtils.logger(AbstractScopeGraphContext.class);

    private final ContextIdentifier identifier;
    private final String persistentIdentifier;
    private final Injector injector;
    private final ReadWriteLock lock;

    protected S state = null;

    public AbstractScopeGraphContext(Injector injector, ContextIdentifier identifier) {
        this.identifier = identifier;
        this.persistentIdentifier = FileUtils.sanitize(identifier.language.id().toString());
        this.injector = injector;
        this.lock = new ReentrantReadWriteLock(true);
    }

    @Override public FileObject location() {
        return identifier.location;
    }

    @Override public IProject project() {
        return identifier.project;
    }

    @Override public ILanguageImpl language() {
        return identifier.language;
    }

    @Override public Injector injector() {
        return injector;
    }

    @Override public IClosableLock read() {
        if (state == null) {
            try (IClosableLock lock = writeLock()) {
                if (state == null) {
                    state = loadOrInitState();
                }
            }
        }
        return readLock();
    }

    private IClosableLock readLock() {
        final Lock readLock = lock.readLock();
        final IClosableLock lock = new ClosableLock(readLock);
        return lock;
    }

    @Override public IClosableLock write() {
        final IClosableLock lock = writeLock();
        if (state == null) {
            state = loadOrInitState();
        }
        return lock;
    }

    private IClosableLock writeLock() {
        final Lock writeLock = lock.writeLock();
        final IClosableLock lock = new ClosableLock(writeLock);
        return lock;
    }

    @Override public void persist() throws IOException {
        if (state == null) {
            return;
        }

        try (IClosableLock lock = readLock()) {
            persistState();
        }
    }

    @Override public void reset() throws IOException {
        try (IClosableLock lock = writeLock()) {
            if (state != null) {
                // state.reset()
                state = null;
            }
        }
    }

    @Override public ContextIdentifier identifier() {
        return identifier;
    }

    @Override public void init() {
        if (state != null) {
            return;
        }
        try (IClosableLock lock = writeLock()) {
            state = initState();
        }
    }

    @Override public void load() {
        if (state != null) {
            return;
        }
        try (IClosableLock lock = writeLock()) {
            state = loadOrInitState();
        }
    }

    @Override public void unload() {
        if (state == null) {
            return;
        }
        try (IClosableLock lock = writeLock()) {
            state = null;
        }
    }

    private S loadOrInitState() {
        try {
            final FileObject contextFile = contextFile();
            try {
                if (contextFile.exists()) {
                    return readContext(contextFile);
                }
            } catch (IOException | ClassNotFoundException e) {
                logger.warn("Load context {} failed.", contextFile, e);
                deleteContextFile(contextFile);
            }
        } catch (IOException e) {
            logger.warn("Failed to locate context.", e);
        }
        return initState();
    }

    protected abstract S initState();

    private FileObject contextFile() throws FileSystemException {
        final CommonPaths paths = new CommonPaths(identifier.location);
        return paths.targetDir().resolveFile("analysis").resolveFile(persistentIdentifier).resolveFile("scopegraph");
    }

    @SuppressWarnings("unchecked") private S readContext(FileObject file) throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(file.getContent().getInputStream())) {
            S fileState = (S) ois.readObject();
            if (fileState == null) {
                throw new IOException("Context file contains null.");
            }
            return fileState;
        }
    }

    private void persistState() {
        try {
            final FileObject contextFile = contextFile();
            try {
                writeContext(contextFile);
            } catch (IOException e) {
                logger.warn("Store context {} failed.", contextFile, e);
            }
        } catch (IOException e) {
            logger.warn("Failed to locate context.", e);
        }
    }

    private void writeContext(FileObject file) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(file.getContent().getOutputStream())) {
            oos.writeObject(state);
        }
    }

    private void deleteContextFile(FileObject file) {
        try {
            file.delete();
        } catch (FileSystemException e) {
            logger.warn("Deleting context {} failed.", file, e);
        }
    }

    @Override public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((identifier == null) ? 0 : identifier.hashCode());
        return result;
    }

    @Override public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        @SuppressWarnings("unchecked") AbstractScopeGraphContext<S> other = (AbstractScopeGraphContext<S>) obj;
        if (!identifier.equals(other.identifier))
            return false;
        return true;
    }

    @Override public String toString() {
        return String.format("scope graph context for %s, %s", identifier.location, identifier.language);
    }

}