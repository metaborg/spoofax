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
                    case "bin":
                    case "include":
                    case "target":
                    case ".cache":
                    case ".settings":
                    case ".mvn":
                        return false;
                }
                break;
            case 3:
                switch(base) {
                    // Ignore editor/java/trans and src-gen/stratego-java/trans.
                    case "trans": {
                        final FileObject parent1 = resource.getParent();
                        if(parent1 != null) {
                            final String parent1base = parent1.getName().getBaseName();
                            if(parent1base.equals("java") || parent1base.equals("stratego-java")) {
                                final FileObject parent2 = parent1.getParent();
                                if(parent2 != null) {
                                    final String parent2base = parent2.getName().getBaseName();
                                    return !(parent2base.equals("editor") || parent2base.equals("src-gen"));
                                }
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
