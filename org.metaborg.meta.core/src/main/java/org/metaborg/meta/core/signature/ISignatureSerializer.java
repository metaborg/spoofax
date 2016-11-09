package org.metaborg.meta.core.signature;

import java.io.IOException;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.util.file.IFileAccess;

public interface ISignatureSerializer {
    Iterable<Signature> read(FileObject location, @Nullable IFileAccess access) throws IOException;

    void write(FileObject location, Iterable<Signature> signatures, @Nullable IFileAccess access) throws IOException;
}
