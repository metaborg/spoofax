package org.metaborg.core.resource;

import java.util.Collection;

import org.apache.commons.vfs2.AllFileSelector;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSelector;
import org.apache.commons.vfs2.FileSystemException;
import org.metaborg.util.iterators.Iterables2;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

public class ResourceUtils {
    public static Iterable<FileObject> find(FileObject base, FileSelector selector) throws FileSystemException {
        final FileObject[] files = base.findFiles(selector);
        if(files == null) {
            return Iterables2.empty();
        }
        return Iterables2.from(files);
    }

    public static Iterable<FileObject> find(FileObject base) throws FileSystemException {
        return find(base, new AllFileSelector());
    }


    public static Iterable<ResourceChange> toChanges(Iterable<FileObject> resources, ResourceChangeKind kind) {
        final int size = Iterables.size(resources);
        final Collection<ResourceChange> changes = Lists.newArrayListWithCapacity(size);
        for(FileObject resource : resources) {
            changes.add(new ResourceChange(resource, kind));
        }
        return changes;
    }
}
