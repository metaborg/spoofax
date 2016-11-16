package org.metaborg.meta.core.signature.generate;

import java.io.IOException;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.meta.core.signature.ISig;
import org.metaborg.util.file.IFileAccess;

public interface IRawSigGen {
    void generate(Iterable<ISig> signatures, FileObject dir, @Nullable IFileAccess access) throws IOException;
}
