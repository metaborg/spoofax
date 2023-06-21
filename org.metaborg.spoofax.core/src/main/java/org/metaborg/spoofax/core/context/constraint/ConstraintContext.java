package org.metaborg.spoofax.core.context.constraint;

import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.commons.io.input.ClassLoaderObjectInputStream;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.metaborg.core.MetaborgRuntimeException;
import org.metaborg.core.build.CommonPaths;
import org.metaborg.core.context.ContextIdentifier;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.project.IProject;
import org.metaborg.util.collection.ImList;
import org.metaborg.util.concurrent.ClosableLock;
import org.metaborg.util.concurrent.IClosableLock;
import org.metaborg.util.file.FileUtils;
import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.LoggerUtils;
import org.metaborg.util.resource.ResourceUtils;
import org.metaborg.util.time.Timer;
import org.spoofax.interpreter.terms.IStrategoTerm;

import com.google.inject.Injector;

public class ConstraintContext implements IConstraintContext {

    private static final ILogger logger = LoggerUtils.logger(ConstraintContext.class);

    private final ContextIdentifier identifier;
    private final String persistentIdentifier;
    private final Injector injector;
    private final ReadWriteLock lock;

    private State state = null;

    public ConstraintContext(Injector injector, ContextIdentifier identifier) {
        this.identifier = identifier;
        this.persistentIdentifier = FileUtils.sanitize(identifier.language.id().toString());
        this.injector = injector;
        this.lock = new ReentrantReadWriteLock(true);
    }

    @Override public String resourceKey(FileObject resource) {
        return ResourceUtils.relativeName(resource.getName(), location().getName(), true);
    }

    @Override public FileObject keyResource(String resource) {
        try {
            return ResourceUtils.resolveFile(location(), resource);
        } catch(FileSystemException e) {
            throw new MetaborgRuntimeException(e);
        }
    }

    @Override public boolean contains(FileObject resource) {
        return state.entries.containsKey(resourceKey(resource));
    }

    @Override public boolean put(FileObject resource, int parseHash, IStrategoTerm analyzedAst, IStrategoTerm analysis,
            IStrategoTerm errors, IStrategoTerm warnings, IStrategoTerm notes, List<String> exceptions) {
        return state.entries.put(resourceKey(resource),
                new Entry(parseHash, analyzedAst, analysis, errors, warnings, notes, exceptions)) != null;
    }

    @Override public IConstraintContext.Entry get(FileObject resource) {
        return state.entries.get(resourceKey(resource));
    }

    @Override public boolean remove(FileObject resource) {
        return state.entries.remove(resourceKey(resource)) != null;
    }

    @Override public Set<Map.Entry<String, IConstraintContext.Entry>> entrySet() {
        return state.entries.entrySet();
    }

    @Override public void clear() {
        state.entries.clear();
    }

    // ----------------------------------------------------------

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
        if(state == null) {
            try(IClosableLock lock = writeLock()) {
                if(state == null) {
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

    public IClosableLock guard() {
        return read();
    }

    @Override public IClosableLock write() {
        final IClosableLock lock = writeLock();
        if(state == null) {
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
        if(state == null) {
            return;
        }

        try(IClosableLock lock = readLock()) {
            persistState();
        }
    }

    @Override public void reset() throws IOException {
        try(IClosableLock lock = writeLock()) {
            if(state != null) {
                state = null;
            }
            final FileObject contextFile = contextFile();
            contextFile.delete();
        }
    }

    @Override public ContextIdentifier identifier() {
        return identifier;
    }

    @Override public void init() {
        if(state != null) {
            return;
        }
        try(IClosableLock lock = writeLock()) {
            state = initState();
        }
    }

    @Override public void load() {
        if(state != null) {
            return;
        }
        try(IClosableLock lock = writeLock()) {
            state = loadOrInitState();
        }
    }

    @Override public void unload() {
        if(state == null) {
            return;
        }
        try(IClosableLock lock = writeLock()) {
            state = null;
        }
    }

    private State loadOrInitState() {
        try {
            final FileObject contextFile = contextFile();
            try {
                if(contextFile.exists()) {
                    return readContext(contextFile);
                }
            } catch(IOException | ClassNotFoundException e) {
                logger.warn("Load context {} failed: {}", contextFile, e.getMessage());
                deleteContextFile(contextFile);
            }
        } catch(IOException e) {
            logger.warn("Failed to locate context: {}", e.getMessage());
        }
        return initState();
    }

    private State initState() {
        return new State();
    }

    private FileObject contextFile() throws FileSystemException {
        final CommonPaths paths = new CommonPaths(identifier.location);
        return paths.targetDir().resolveFile("analysis").resolveFile(persistentIdentifier).resolveFile("constraint");
    }

    private State readContext(FileObject file) throws IOException, ClassNotFoundException, ClassCastException {
        try(ObjectInputStream ois =
                new ClassLoaderObjectInputStream(getClass().getClassLoader(), file.getContent().getInputStream())) {
            State fileState;
            try {
                fileState = (State) ois.readObject();
            } catch(NotSerializableException ex) {
                logger.warn("Context could not be read: {}", ex.getMessage());
                fileState = initState();
            } catch(Exception ex) {
                final String msg = logger.format("Context file could not be read: {}", ex.getMessage());
                throw new IOException(msg);
            }
            if(fileState == null) {
                throw new IOException("Context file is empty.");
            }
            return fileState;
        }
    }

    private void persistState() {
        try {
            final FileObject contextFile = contextFile();
            try {
                writeContext(contextFile);
            } catch(IOException e) {
                logger.warn("Store context {} failed: {}", contextFile, e.getMessage());
            }
        } catch(IOException e) {
            logger.warn("Failed to locate context: {}", e.getMessage());
        }
    }

    private void writeContext(FileObject file) throws IOException {
        Timer timer = new Timer(true);
        try(ObjectOutputStream oos = new ObjectOutputStream(file.getContent().getOutputStream())) {
            oos.writeObject(state);
        } catch(NotSerializableException ex) {
            logger.warn("Constraint context persistence not serializable: {}", ex.getMessage());
        } catch(Exception ex) {
            throw new IOException("Context file could not be written.", ex);
        } finally {
            logger.debug("Context written in {} s", timer.stop() / 1_000_000_000d);
        }
    }

    private void deleteContextFile(FileObject file) {
        try {
            file.delete();
        } catch(FileSystemException e) {
            logger.warn("Deleting context {} failed: {}", file, e.getMessage());
        }
    }

    @Override public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((identifier == null) ? 0 : identifier.hashCode());
        return result;
    }

    @Override public boolean equals(Object obj) {
        if(this == obj)
            return true;
        if(obj == null)
            return false;
        if(getClass() != obj.getClass())
            return false;
        final ConstraintContext other = (ConstraintContext) obj;
        if(!identifier.equals(other.identifier))
            return false;
        return true;
    }

    @Override public String toString() {
        return String.format("Constraint context for %s, %s", identifier.location, identifier.language);
    }

    private static class State implements Serializable {

        private static final long serialVersionUID = 1L;

        public final Map<String, IConstraintContext.Entry> entries;

        public State() {
            this.entries = new HashMap<>();
        }

    }

    private static class Entry implements IConstraintContext.Entry, Serializable {

        private static final long serialVersionUID = 1L;

        public final int parseHash;
        public transient IStrategoTerm analyzedAst;
        public final IStrategoTerm analysis;
        public final IStrategoTerm errors;
        public final IStrategoTerm warnings;
        public final IStrategoTerm notes;
        public final List<String> exceptions;

        Entry(int parseHash, IStrategoTerm analyzedAst, IStrategoTerm analysis, IStrategoTerm errors,
                IStrategoTerm warnings, IStrategoTerm notes, List<String> exceptions) {
            this.parseHash = parseHash;
            this.analyzedAst = analyzedAst;
            this.analysis = analysis;
            this.errors = errors;
            this.warnings = warnings;
            this.notes = notes;
            this.exceptions = ImList.Immutable.copyOf(exceptions);
        }

        @Override public int parseHash() {
            return parseHash;
        }

        @Override public IStrategoTerm analyzedAst() {
            return analyzedAst;
        }

        @Override public IStrategoTerm analysis() {
            return analysis;
        }

        @Override public IStrategoTerm errors() {
            return errors;
        }

        @Override public IStrategoTerm warnings() {
            return warnings;
        }

        @Override public IStrategoTerm notes() {
            return notes;
        }

        @Override public List<String> exceptions() {
            return exceptions;
        }

    }

}
