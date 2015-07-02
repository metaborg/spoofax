package org.metaborg.spoofax.core.context;

import java.io.File;
import java.net.URI;

import org.apache.commons.vfs2.AllFileSelector;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.metaborg.core.context.ContextIdentifier;
import org.metaborg.core.context.IContext;
import org.metaborg.core.context.IContextInternal;
import org.metaborg.core.language.ILanguage;
import org.metaborg.core.resource.IResourceService;
import org.metaborg.runtime.task.engine.TaskManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spoofax.interpreter.library.index.IndexManager;

public class SpoofaxContext implements IContext, IContextInternal {
    private static final long serialVersionUID = 4177944175684703453L;
    private static final Logger logger = LoggerFactory.getLogger(SpoofaxContext.class);

    private final URI locationURI;
    private final ContextIdentifier identifier;


    public SpoofaxContext(IResourceService resourceService, ContextIdentifier identifier) {
        this.identifier = identifier;
        this.locationURI = locationURI(resourceService);
    }


    @Override public ContextIdentifier identifier() {
        return identifier;
    }

    @Override public FileObject location() {
        return identifier.location;
    }

    @Override public ILanguage language() {
        return identifier.language;
    }



    @Override public void clean() {
        try {
            final FileObject cacheDir = identifier.location.resolveFile(".cache");
            cacheDir.delete(new AllFileSelector());
        } catch(FileSystemException e) {
            final String message = String.format("Cannot delete cache directory in %s", this);
            logger.error(message, e);
        }

        if(locationURI == null) {
            logger.error("{} does not reside on the local file system, cannot clean the index and task engine", this);
            return;
        }

        final IndexManager indexManager = IndexManager.getInstance();
        indexManager.resetIndex(locationURI);
        indexManager.unloadIndex(locationURI);

        final TaskManager taskManager = TaskManager.getInstance();
        taskManager.resetTaskEngine(locationURI);
        taskManager.unloadTaskEngine(locationURI);
    }

    @Override public void initialize() {

    }

    @Override public void unload() {

    }


    private URI locationURI(IResourceService resourceService) {
        final File localLocation = resourceService.localPath(identifier.location);
        if(localLocation == null) {
            return null;
        }
        return localLocation.toURI();
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
        final SpoofaxContext other = (SpoofaxContext) obj;
        if(!identifier.equals(other.identifier))
            return false;
        return true;
    }

    @Override public String toString() {
        return String.format("Context for %s, %s", identifier.location, identifier.language);
    }
}
