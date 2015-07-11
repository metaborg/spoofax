package org.metaborg.spoofax.core.resource;

import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSelectInfo;
import org.apache.commons.vfs2.FileSelector;

public class SpoofaxIgnoresSelector implements FileSelector {
    @Override public boolean includeFile(FileSelectInfo fileInfo) throws Exception {
        return true;
    }

    @Override public boolean traverseDescendents(FileSelectInfo fileInfo) throws Exception {
        final int depth = fileInfo.getDepth();
        final FileObject resource = fileInfo.getFile();
        final FileName name = resource.getName();
        final String base = name.getBaseName();

        switch(depth) {
            case 1:
                switch(base) {
                    case "include":
                    case "target":
                    case ".cache":
                        return false;
                }
                break;
            case 3:
                switch(base) {
                    case "trans": {
                        final FileObject parent1 = resource.getParent();
                        if(parent1 != null && parent1.getName().getBaseName().equals("java")) {
                            final FileObject parent2 = parent1.getParent();
                            if(parent2 != null) {
                                return !parent2.getName().getBaseName().equals("editor");
                            }
                        }
                        break;
                    }
                }
                break;
        }

        return true;
    }
}
