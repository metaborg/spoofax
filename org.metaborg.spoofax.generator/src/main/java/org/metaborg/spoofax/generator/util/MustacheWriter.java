package org.metaborg.spoofax.generator.util;

import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;

import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;

public class MustacheWriter {
    private final FileObject root;
    private final Object[] objects;
    private final MustacheFactory factory;


    public MustacheWriter(FileObject root, Object[] objs, Class<?> cls) {
        this.root = root;
        this.objects = objs;
        this.factory = new StrictMustacheFactory(new ClassResolver(cls));
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


    private void write(Mustache mustache, FileObject dest, boolean overwrite) throws FileSystemException {
        if(dest.exists() && !overwrite) {
            return;
        }
        dest.createFile();
        try(final PrintWriter writer = new PrintWriter(dest.getContent().getOutputStream())) {
            mustache.execute(writer, objects);
        }
    }

    private String writeString(String template) {
        final Mustache dest = factory.compile(new StringReader(template), "nameOf(" + template + ")");
        return dest.execute(new StringWriter(), objects).toString();
    }
}
