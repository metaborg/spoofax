package org.metaborg.meta.core.mustache;

import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;

import javax.annotation.Nullable;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.metaborg.util.file.IFileAccess;

import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;

public class MustacheWriter {
    private final FileObject root;
    private final Object[] baseObjects;
    private final MustacheFactory factory;
    private final @Nullable IFileAccess access;


    public MustacheWriter(FileObject root, Object[] baseObjects, Class<?> clazz, @Nullable IFileAccess access) {
        this.root = root;
        this.baseObjects = baseObjects;
        this.factory = new StrictMustacheFactory(new ClassResolver(clazz));
        this.access = access;
    }


    public boolean exists(String srcNameTemplate, Object... extraObjects) throws FileSystemException {
        final Object[] objects;
        if(extraObjects.length == 0) {
            objects = baseObjects;
        } else {
            objects = ArrayUtils.addAll(baseObjects, extraObjects);
        }
        final String srcName = writeString(srcNameTemplate, objects);
        final FileObject resource = root.resolveFile(srcName);
        return resource.exists();
    }


    public FileObject write(String nameTemplate, boolean overwrite, Object... extraObjects) throws FileSystemException {
        final Object[] objects;
        if(extraObjects.length == 0) {
            objects = baseObjects;
        } else {
            objects = ArrayUtils.addAll(baseObjects, extraObjects);
        }
        final String srcName = nameTemplate;
        final String dstName = writeString(nameTemplate, objects);
        final Mustache content = factory.compile(srcName);
        return write(content, root.resolveFile(dstName), overwrite, objects);
    }

    public FileObject writeResolve(String nameTemplate, boolean overwrite, Object... extraObjects)
        throws FileSystemException {
        final Object[] objects;
        if(extraObjects.length == 0) {
            objects = baseObjects;
        } else {
            objects = ArrayUtils.addAll(baseObjects, extraObjects);
        }
        final String name = writeString(nameTemplate, objects);
        final Mustache content = factory.compile(name);
        return write(content, root.resolveFile(name), overwrite, objects);
    }


    public FileObject write(String srcName, String dstNameTemplate, boolean overwrite, Object... extraObjects)
        throws FileSystemException {
        final Object[] objects;
        if(extraObjects.length == 0) {
            objects = baseObjects;
        } else {
            objects = ArrayUtils.addAll(baseObjects, extraObjects);
        }
        final Mustache content = factory.compile(srcName);
        final String dstName = writeString(dstNameTemplate, objects);
        return write(content, root.resolveFile(dstName), overwrite, objects);
    }

    public FileObject writeResolve(String srcNameTemplate, String dstNameTemplate, boolean overwrite,
        Object... extraObjects) throws FileSystemException {
        final Object[] objects;
        if(extraObjects.length == 0) {
            objects = baseObjects;
        } else {
            objects = ArrayUtils.addAll(baseObjects, extraObjects);
        }
        final String srcName = writeString(srcNameTemplate, objects);
        final String dstName = writeString(dstNameTemplate, objects);
        final Mustache content = factory.compile(srcName);
        return write(content, root.resolveFile(dstName), overwrite, objects);
    }


    private FileObject write(Mustache mustache, FileObject dst, boolean overwrite, Object[] objects)
        throws FileSystemException {
        if(dst.exists() && !overwrite) {
            return dst;
        }
        dst.createFile();
        try(final PrintWriter writer = new PrintWriter(dst.getContent().getOutputStream())) {
            mustache.execute(writer, objects);
        }
        if(access != null) {
            access.write(dst);
        }
        return dst;
    }

    private String writeString(String template, Object[] objects) {
        final Mustache dest = factory.compile(new StringReader(template), "nameOf(" + template + ")");
        return dest.execute(new StringWriter(), objects).toString();
    }
}
