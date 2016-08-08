package org.metaborg.spoofax.core.context.scopegraph;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.commons.vfs2.AllFileSelector;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.metaborg.core.build.CommonPaths;
import org.metaborg.core.context.ContextIdentifier;
import org.metaborg.core.context.IContextInternal;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.project.IProject;
import org.metaborg.scopegraph.context.IScopeGraphContext;
import org.metaborg.util.concurrent.ClosableLock;
import org.metaborg.util.concurrent.IClosableLock;
import org.metaborg.util.file.FileUtils;
import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.LoggerUtils;

import com.google.common.collect.Maps;
import com.google.inject.Injector;

public class ScopeGraphContext implements IScopeGraphContext<ScopeGraphUnit>, IContextInternal {
    private static final ILogger logger = LoggerUtils.logger(ScopeGraphContext.class);

    private final ContextIdentifier identifier;
    private final String persistentIdentifier;
    private final Injector injector;
    private final ReadWriteLock lock;

    private Map<String,ScopeGraphUnit> units = null;

 
    public ScopeGraphContext(Injector injector, ContextIdentifier identifier) {
        this.identifier = identifier;
        this.persistentIdentifier = FileUtils.sanitize(identifier.language.id().toString());
        this.injector = injector;
        this.lock = new ReentrantReadWriteLock(true);
    }


    @Override
    public FileObject location() {
        return identifier.location;
    }

    @Override
    public IProject project() {
        return identifier.project;
    }

    @Override
    public ILanguageImpl language() {
        return identifier.language;
    }

    @Override
    public Injector injector() {
        return injector;
    }

    @Override
    public IClosableLock read() {
        if(units == null) {
            try(IClosableLock lock = writeLock()) {
                if(units == null) {
                    units = loadOrInitUnits();
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
    
    @Override
    public IClosableLock write() {
        final IClosableLock lock = writeLock();
        if(units == null) {
            units = loadOrInitUnits();
        }
        return lock;
    }

    private IClosableLock writeLock() {
        final Lock writeLock = lock.writeLock();
        final IClosableLock lock = new ClosableLock(writeLock);
        return lock;
    }
    
    @Override
    public void persist() throws IOException {
        if(units == null) {
            return;
        }
        
        try(IClosableLock lock = readLock()) {
            persistUnits();
        }
    }

    @Override
    public void reset() throws IOException {
        try(IClosableLock lock = writeLock()) {
            units.clear();
            final FileObject cacheDir = identifier.location.resolveFile(".cache");
            cacheDir.delete(new AllFileSelector());
        }
    }

    @Override
    public ContextIdentifier identifier() {
        return identifier;
    }

    @Override
    public void init() {
        if(units != null) {
            return;
        }
        try(IClosableLock lock = writeLock()) {
            units = initUnits();
        }
    }

    @Override
    public void load() {
        if(units != null) {
            return;
        }
        try(IClosableLock lock = writeLock()) {
            units = loadOrInitUnits();
        }
    }

    @Override
    public void unload() {
        if(units == null) {
            return;
        }
        try(IClosableLock lock = writeLock()) {
            units = null;
        }
    }


    private Map<String, ScopeGraphUnit> loadOrInitUnits() {
        try {
            final FileObject contextFile = contextFile();
            try {
                if(contextFile.exists()) {
                    return readContext(contextFile);
                }
            } catch (IOException | ClassNotFoundException e) {
                logger.warn("Load context {} failed.",contextFile,e);
                deleteContextFile(contextFile);
            }
        } catch (IOException e) {
            logger.warn("Failed to locate context.",e);
        }
        return initUnits();
    }
    
    private Map<String, ScopeGraphUnit> initUnits() {
        return Maps.newHashMap();
    }
    
    private FileObject contextFile() throws FileSystemException {
        final CommonPaths paths = new CommonPaths(identifier.location);
        return paths.targetDir()
                .resolveFile("analysis")
                .resolveFile(persistentIdentifier)
                .resolveFile("scopegraph");
    }
    
    @SuppressWarnings("unchecked")
    private Map<String, ScopeGraphUnit> readContext(FileObject file) throws IOException, ClassNotFoundException {
        try(ObjectInputStream ois = new ObjectInputStream(file.getContent().getInputStream())) {
            return (Map<String, ScopeGraphUnit>) ois.readObject();
        }
    }
 
    private void persistUnits() {
        try {
            final FileObject contextFile = contextFile();
            try {
                writeContext(contextFile);
            } catch (IOException e) {
                logger.warn("Store context {} failed.",contextFile,e);
            }
        } catch (IOException e) {
            logger.warn("Failed to locate context.",e);
        }
    }
    
    private void writeContext(FileObject file) throws IOException {
        try(ObjectOutputStream oos = new ObjectOutputStream(file.getContent().getOutputStream())) {
            oos.writeObject(units);
        }
    }
    
    private void deleteContextFile(FileObject file) {
        try {
            file.delete();
        } catch (FileSystemException e) {
            logger.warn("Deleting context {} failed.",file,e);
        }
    }
    
    public ScopeGraphUnit getOrCreateUnit(String source) {
        ScopeGraphUnit unit;
        if((unit = units.get(source)) == null) {
            units.put(source, (unit = new ScopeGraphUnit(source)));
        }
        return unit;
    }

    @Override
    public ScopeGraphUnit unit(String source) {
        return units.get(source);
    }

    @Override   
    public Collection<ScopeGraphUnit> units() {
        return units.values();
    }

    @Override
    public void removeUnit(String source) {
        units.remove(source);
    }

    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((identifier == null) ? 0 : identifier.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ScopeGraphContext other = (ScopeGraphContext) obj;
        if (!identifier.equals(other.identifier))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return String.format("scope graph context for %s, %s", identifier.location, identifier.language);
    }

}