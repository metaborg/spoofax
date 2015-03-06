package org.metaborg.spoofax.core.resource;

import java.io.File;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;

public interface ILocalFileProvider {
    public abstract @Nullable File localFile(FileObject resource);

    public String scheme();
}
