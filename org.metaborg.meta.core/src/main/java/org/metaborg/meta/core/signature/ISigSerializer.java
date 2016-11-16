package org.metaborg.meta.core.signature;

import java.io.IOException;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.util.file.IFileAccess;

public interface ISigSerializer {
    Iterable<ISig> read(FileObject location, @Nullable IFileAccess access) throws IOException;

    void write(FileObject location, Iterable<ISig> signatures, @Nullable IFileAccess access) throws IOException;
}
