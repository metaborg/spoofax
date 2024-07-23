package org.metaborg.core.resource;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSelectInfo;
import org.apache.commons.vfs2.FileSelector;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.provider.zip.ZipFileObject;

public class ModifiedFileSelector implements FileSelector {
    private final FileObject copyToLocation;

    public ModifiedFileSelector(FileObject copyToLocation) {
        this.copyToLocation = copyToLocation;
    }

    @Override public boolean includeFile(FileSelectInfo fileInfo) throws Exception {
        final FileObject originalFile = fileInfo.getFile();
        if(!originalFile.isFile()) {
            // if not a file but a directory or something like that instead, just include it in the traversal
            // Note that this is necessary to work around https://issues.apache.org/jira/browse/VFS-399
            return true;
        }
        final FileObject copyToCandidate = copyToLocation.resolveFile(
            fileInfo.getBaseFolder().getName().getRelativeName(originalFile.getName()));

        return !(copyToCandidate.exists()
            && copyToCandidate.getContent().getLastModifiedTime() > originalFile.getContent().getLastModifiedTime());
    }

    @Override public boolean traverseDescendents(FileSelectInfo fileInfo) throws Exception {
        return true;
    }
}
