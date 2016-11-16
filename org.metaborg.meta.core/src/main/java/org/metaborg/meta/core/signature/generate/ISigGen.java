package org.metaborg.meta.core.signature.generate;

import java.io.IOException;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.meta.core.signature.ISort;
import org.metaborg.meta.core.signature.ISortArg;
import org.metaborg.util.file.IFileAccess;

public interface ISigGen {
    void start(FileObject dir, @Nullable IFileAccess access) throws IOException;

    void generateSort(String sort, Iterable<ISort> injections) throws IOException;

    void generateConstructor(String cons, Iterable<String> sorts, Iterable<ISortArg> args, int arity)
        throws IOException;
}
