package org.metaborg.spoofax.core.resource;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSelector;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * Resource change monitor implementation that can persist resource modification dates, which can be used to find
 * resource changes between different sessions.
 * 
 * When calling {@link #update()}, the current state of resources is compared against the last known state. If there is
 * no last known state, all resources will change with kind {@link ResourceChangeKind#Create}.
 * 
 * This class is NOT thread safe.
 */
public class OfflineResourceChangeMonitor {
    private final FileObject watchDir;
    private final FileObject storageDir;
    private final FileSelector selector;
    private final IResourceService resourceService;

    private Map<String, Long> modificationDates = Maps.newHashMap();
    private final Kryo serializer = new Kryo();


    public OfflineResourceChangeMonitor(final FileObject watchDir, final FileObject storageDir,
        final FileSelector selector, IResourceService resourceService) {
        this.watchDir = watchDir;
        this.storageDir = storageDir;
        this.selector = selector;
        this.resourceService = resourceService;
    }


    /**
     * Compares the current state of resources against the last know state, updates the last known state to the current
     * state, and pushes changes to the {@link #changes()} observable.
     * 
     * @return Iterable over all changes.
     * 
     * @throws FileSystemException
     *             when resolving resources from last known state fails.
     */
    public Iterable<IResourceChange> update() throws FileSystemException {
        final Collection<IResourceChange> changes = Lists.newLinkedList();
        final Set<String> oldResourceNames = Sets.newHashSet(modificationDates.keySet());
        final FileObject[] newResources = watchDir.findFiles(selector);

        for(FileObject resource : newResources) {
            final String name = resource.getName().getPath();
            final Long oldModificationDate = modificationDates.get(name);
            final long newModificationDate = resource.getContent().getLastModifiedTime();
            if(oldModificationDate == null) {
                changes.add(new ResourceChange(resource, ResourceChangeKind.Create, null, null));
            } else {
                if(newModificationDate > oldModificationDate) {
                    changes.add(new ResourceChange(resource, ResourceChangeKind.Modify, null, null));
                }
            }
            oldResourceNames.remove(name);
            modificationDates.put(name, newModificationDate);
        }
        for(String name : oldResourceNames) {
            final FileObject resource = resourceService.resolve(name);
            changes.add(new ResourceChange(resource, ResourceChangeKind.Delete, null, null));
            modificationDates.remove(name);
        }

        return changes;
    }

    /**
     * Resets the last known state, causing the next update to produce {@link ResourceChangeKind#Create} changes for all
     * resources.
     */
    public void reset() {
        modificationDates.clear();
    }

    /**
     * Reads modification dates from persisted resource into last known state. Does nothing if nothing was persisted
     * before.
     * 
     * @throws FileSystemException
     *             when reading from the resource fails.
     */
    @SuppressWarnings("unchecked") public void read() throws FileSystemException {
        final FileObject storageFile = storageFile();
        if(storageFile.getType() != FileType.FILE) {
            return;
        }
        try(final Input input = new Input(storageFile.getContent().getInputStream())) {
            modificationDates = (Map<String, Long>) serializer.readObject(input, HashMap.class);
        }
    }

    /**
     * Writes last known state into persisted storage.
     * 
     * @throws FileSystemException
     *             when creating the resource into persisted storage fails.
     * @throws FileSystemException
     *             when writing to the resource fails.
     */
    public void write() throws FileSystemException {
        final FileObject storageFile = storageFile();
        storageFile.createFile();
        try(final Output output = new Output(storageFile.getContent().getOutputStream())) {
            serializer.writeObject(output, modificationDates);
        }
    }

    private FileObject storageFile() throws FileSystemException {
        return storageDir.resolveFile("offline-resource-change-monitor.obj");
    }
}