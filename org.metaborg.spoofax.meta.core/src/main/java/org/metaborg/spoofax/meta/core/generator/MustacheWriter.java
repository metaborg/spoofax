package org.metaborg.spoofax.meta.core.generator;

import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.metaborg.util.file.FileAccess;

import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;

public class MustacheWriter {
    private final FileObject root;
    private final Object[] objects;
    private final MustacheFactory factory;
    private final @Nullable FileAccess access;


    public MustacheWriter(FileObject root, Object[] objects, Class<?> clazz, @Nullable FileAccess access) {
        this.root = root;
        this.objects = objects;
        this.factory = new StrictMustacheFactory(new ClassResolver(clazz));
        this.access = access;
    }


    public boolean exists(String srcNameTemplate) throws FileSystemException {
        final String srcName = writeString(srcNameTemplate);
        final FileObject resource = root.resolveFile(srcName);
        return resource.exists();
    }


    public void write(String nameTemplate, boolean overwrite) throws FileSystemException {
        final String srcName = nameTemplate;
        final String dstName = writeString(nameTemplate);
        final Mustache content = factory.compile(srcName);
        write(content, root.resolveFile(dstName), overwrite);
    }

    public void writeResolve(String nameTemplate, boolean overwrite) throws FileSystemException {
        final String name = writeString(nameTemplate);
        final Mustache content = factory.compile(name);
        write(content, root.resolveFile(name), overwrite);
    }


    public void write(String srcName, String dstName, boolean overwrite) throws FileSystemException {
        final Mustache content = factory.compile(srcName);
        write(content, root.resolveFile(dstName), overwrite);
    }

    public void writeResolve(String srcNameTemplate, String dstNameTemplate, boolean overwrite)
        throws FileSystemException {
        final String srcName = writeString(srcNameTemplate);
        final String dstName = writeString(dstNameTemplate);
        final Mustache content = factory.compile(srcName);
        write(content, root.resolveFile(dstName), overwrite);
    }


    private void write(Mustache mustache, FileObject dst, boolean overwrite) throws FileSystemException {
        if(dst.exists() && !overwrite) {
            return;
        }
        dst.createFile();
        try(final PrintWriter writer = new PrintWriter(dst.getContent().getOutputStream())) {
            mustache.execute(writer, objects);
        }
        if(access != null) {
            access.addWrite(dst);
        }
    }

    private String writeString(String template) {
        final Mustache dest = factory.compile(new StringReader(template), "nameOf(" + template + ")");
        return dest.execute(new StringWriter(), objects).toString();
    }
}
