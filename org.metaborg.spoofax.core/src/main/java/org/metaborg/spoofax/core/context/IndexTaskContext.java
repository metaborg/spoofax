package org.metaborg.spoofax.core.context;

import java.io.IOException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.metaborg.core.build.CommonPaths;
import org.metaborg.core.context.ContextIdentifier;
import org.metaborg.core.context.IContext;
import org.metaborg.core.context.IContextInternal;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.project.IProject;
import org.metaborg.runtime.task.engine.ITaskEngine;
import org.metaborg.runtime.task.engine.TaskManager;
import org.metaborg.spoofax.core.terms.ITermFactoryService;
import org.metaborg.util.concurrent.ClosableLock;
import org.metaborg.util.concurrent.IClosableLock;
import org.metaborg.util.file.FileUtils;
import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.LoggerUtils;
import org.spoofax.interpreter.library.index.IIndex;
import org.spoofax.interpreter.library.index.IndexManager;
import org.spoofax.interpreter.terms.ITermFactory;

import com.google.inject.Injector;

public class IndexTaskContext implements IContext, IContextInternal, IIndexTaskContext {
    private static final ILogger logger = LoggerUtils.logger(IndexTaskContext.class);

    private final Injector injector;

    private final ITermFactory termFactory;
    private final ReadWriteLock lock;
    private final String persistentIdentifier;

    private final ContextIdentifier identifier;

    private IIndex index;
    private ITaskEngine taskEngine;


    public IndexTaskContext(Injector injector, ITermFactoryService termFactoryService, ContextIdentifier identifier) {
        this.injector = injector;

        this.termFactory = termFactoryService.get(identifier.language, null, false);
        this.lock = new ReentrantReadWriteLock(true);
        this.persistentIdentifier = FileUtils.sanitize(identifier.language.id().toString());
        this.identifier = identifier;
    }


    @Override public ContextIdentifier identifier() {
        return identifier;
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


    @Override public @Nullable IIndex index() {
        return index;
    }

    @Override public @Nullable ITaskEngine taskEngine() {
        return taskEngine;
    }


    @Override public IClosableLock read() {
        if(index == null || taskEngine == null) {
            // THREADING: temporarily acquire a write lock when initializing the index, need exclusive access.
            try(IClosableLock lock = writeLock()) {
                /*
                 * THREADING: re-check if index/task engine are still null now that we have exclusive access, there
                 * could have been a context switch before acquiring the lock. Check is also needed because the null
                 * check before is disjunct.
                 */
                if(index == null) {
                    index = loadIndex();
                }
                if(taskEngine == null) {
                    taskEngine = loadTaskEngine();
                }
            }
        }

        index.recover();
        taskEngine.recover();

        return readLock();
    }

    private IClosableLock readLock() {
        final Lock readLock = lock.readLock();
        final IClosableLock lock = new ClosableLock(readLock);
        return lock;
    }

    @Override public IClosableLock write() {
        final IClosableLock lock = writeLock();

        if(index == null) {
            index = loadIndex();
        }
        if(taskEngine == null) {
            taskEngine = loadTaskEngine();
        }

        index.recover();
        taskEngine.recover();

        return lock;
    }

    private IClosableLock writeLock() {
        final Lock writeLock = lock.writeLock();
        final IClosableLock lock = new ClosableLock(writeLock);
        return lock;
    }


    @Override public void persist() throws IOException {
        if(index == null && taskEngine == null) {
            return;
        }

        try(IClosableLock lock = readLock()) {
            if(index != null) {
                IndexManager.write(index, indexFile(), termFactory);
            }
            if(taskEngine != null) {
                TaskManager.write(taskEngine, taskEngineFile(), termFactory);
            }
        }
    }

    @Override public void reset() throws FileSystemException {
        try(IClosableLock lock = writeLock()) {
            if(index != null) {
                index.reset();
                index = null;
            }

            if(taskEngine != null) {
                taskEngine.reset();
                taskEngine = null;
            }

            final FileObject indexFile = indexFile();
            indexFile.delete();
            final FileObject taskEngineFile = taskEngineFile();
            taskEngineFile.delete();
        }
    }


    @Override public void init() {
        if(index != null && taskEngine != null) {
            return;
        }

        try(IClosableLock lock = writeLock()) {
            if(index == null) {
                index = initIndex();
            }
            if(taskEngine == null) {
                taskEngine = initTaskEngine();
            }
        }
    }

    @Override public void load() {
        if(index != null && taskEngine != null) {
            return;
        }

        try(IClosableLock lock = writeLock()) {
            if(index == null) {
                index = loadIndex();
            }
            if(taskEngine == null) {
                taskEngine = loadTaskEngine();
            }
        }
    }

    @Override public void unload() {
        if(index == null && taskEngine == null) {
            return;
        }

        try(IClosableLock lock = writeLock()) {
            index = null;
            taskEngine = null;
        }
    }


    private FileObject indexFile() throws FileSystemException {
        final CommonPaths paths = new CommonPaths(identifier.location);
        return paths.targetDir().resolveFile("analysis").resolveFile(persistentIdentifier).resolveFile("index");
    }

    private IIndex initIndex() {
        return IndexManager.create(termFactory);
    }

    private IIndex loadIndex() {
        try {
            final FileObject indexFile = indexFile();
            if(indexFile.exists()) {
                try {
                    final IIndex index = IndexManager.read(indexFile, termFactory);
                    return index;
                } catch(Exception e) {
                    logger.error("Loading index from {} failed, deleting that file and returning an empty index. "
                        + "Clean the project to reanalyze", e, indexFile);
                    deleteIndexFile(indexFile);
                }
            }
        } catch(FileSystemException e) {
            logger.error(
                "Locating index file for {} failed, returning an empty index. " + "Clean the project to reanalyze", e,
                this);
        }
        return initIndex();
    }

    private void deleteIndexFile(FileObject file) {
        try {
            file.delete();
        } catch(Exception e) {
            logger.error("Deleting index file {} failed, please delete the file manually", e, file);
        }
    }


    private FileObject taskEngineFile() throws FileSystemException {
        final CommonPaths paths = new CommonPaths(identifier.location);
        return paths.targetDir().resolveFile("analysis").resolveFile(persistentIdentifier).resolveFile("tasks");
    }

    private ITaskEngine initTaskEngine() {
        return TaskManager.create(termFactory);
    }

    private ITaskEngine loadTaskEngine() {
        try {
            final FileObject taskEngineFile = taskEngineFile();
            if(taskEngineFile.exists()) {
                try {
                    final ITaskEngine taskEngine = TaskManager.read(taskEngineFile, termFactory);
                    return taskEngine;
                } catch(Exception e) {
                    logger.error(
                        "Loading task engine from {} failed, deleting that file and returning an empty task engine. "
                            + "Clean the project to reanalyze",
                        e, taskEngineFile);
                    deleteTaskEngineFile(taskEngineFile);
                }
            }
        } catch(FileSystemException e) {
            logger.error("Locating task engine file for {} failed, returning an empty task engine. "
                + "Clean the project to reanalyze", e, this);
        }
        return initTaskEngine();
    }

    private void deleteTaskEngineFile(FileObject file) {
        try {
            file.delete();
        } catch(Exception e) {
            logger.error("Deleting task engine file {} failed, please delete the file manually", e, file);
        }
    }


    @Override public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + identifier.hashCode();
        return result;
    }

    @Override public boolean equals(Object obj) {
        if(this == obj)
            return true;
        if(obj == null)
            return false;
        if(getClass() != obj.getClass())
            return false;
        final IndexTaskContext other = (IndexTaskContext) obj;
        if(!identifier.equals(other.identifier))
            return false;
        return true;
    }

    @Override public String toString() {
        return String.format("context for %s, %s", identifier.location, identifier.language);
    }
}
