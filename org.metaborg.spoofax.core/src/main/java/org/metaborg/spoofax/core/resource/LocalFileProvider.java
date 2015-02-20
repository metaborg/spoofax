package org.metaborg.spoofax.core.resource;

import java.io.File;

import org.apache.commons.vfs2.FileObject;

public class LocalFileProvider implements ILocalFileProvider {
    public static final String scheme = "file";

    @Override public File localFile(FileObject resource) {
        return new File(resource.getName().getPath());
    }

    @Override public String scheme() {
        return scheme;
    }
}
