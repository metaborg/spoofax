package org.metaborg.core.resource;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSelectInfo;
import org.apache.commons.vfs2.FileSelector;

public class ModifiedFileSelector implements FileSelector {
    private final FileObject copyToLocation;

    public ModifiedFileSelector(FileObject copyToLocation) {
        this.copyToLocation = copyToLocation;
    }

    @Override public boolean includeFile(FileSelectInfo fileInfo) throws Exception {
        final FileObject originalFile = fileInfo.getFile();
        final FileObject copyToCandidate = copyToLocation.resolveFile(
            fileInfo.getBaseFolder().getName().getRelativeName(originalFile.getName()));
        return !(copyToCandidate.exists()
            && copyToCandidate.getContent().getLastModifiedTime() > originalFile.getContent()
            .getLastModifiedTime());
    }

    @Override public boolean traverseDescendents(FileSelectInfo fileInfo) throws Exception {
        return true;
    }
}
