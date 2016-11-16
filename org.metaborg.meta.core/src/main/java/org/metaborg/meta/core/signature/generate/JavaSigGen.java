package org.metaborg.meta.core.signature.generate;

import java.io.IOException;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.meta.core.mustache.MustacheWriter;
import org.metaborg.meta.core.signature.ISort;
import org.metaborg.meta.core.signature.ISortArg;
import org.metaborg.util.file.IFileAccess;

public class JavaSigGen implements ISigGen {
    private @Nullable IFileAccess access;
    @SuppressWarnings("unused") private MustacheWriter writer;


    @Override public void start(FileObject dir, @Nullable IFileAccess access) {
        this.access = access;
        this.writer = new MustacheWriter(dir, new Object[0], getClass(), access);
    }

    @Override public void generateSort(final String sort, final Iterable<ISort> injections) throws IOException {
        // TODO
    }

    @Override public void generateConstructor(String cons, Iterable<String> sorts, Iterable<ISortArg> args, int arity)
        throws IOException {
        // TODO
    }
}
