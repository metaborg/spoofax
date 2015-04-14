package org.metaborg.spoofax.core.resource;

import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSelectInfo;
import org.apache.commons.vfs2.FileSelector;
import org.metaborg.util.resource.FilterFileSelector;

public class SpoofaxIgnoredDirectories {
    public static boolean ignoreResource(FileObject resource) {
        final FileName name = resource.getName();
        final String path = name.getPath();
        return path.contains("/editor/java/trans/") || path.contains("/include/") || path.contains("/target/")
            || path.contains("/.cache/");
    }

    public static FileSelector ignoreFileSelector(FileSelector includeSelector) {
        return new FilterFileSelector(includeSelector, new FileSelector() {
            @Override public boolean traverseDescendents(FileSelectInfo fileInfo) throws Exception {
                return ignoreResource(fileInfo.getFile());
            }

            @Override public boolean includeFile(FileSelectInfo fileInfo) throws Exception {
                return ignoreResource(fileInfo.getFile());
            }
        });
    }
}
