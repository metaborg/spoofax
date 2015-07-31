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


    public void write(String name, boolean force) throws FileSystemException {
        write(name, name, force);
    }

    public void write(String name, String ifNameTemplate) throws FileSystemException {
        write(name, name, ifNameTemplate);
    }

    public void write(String srcName, String dstNameTemplate, String ifNameTemplate) throws FileSystemException {
        final String ifName = writeString(ifNameTemplate);
        if(root.resolveFile(ifName).exists()) {
            write(srcName, dstNameTemplate, true);
        }
    }

    public void write(String srcName, String dstNameTemplate, boolean force) throws FileSystemException {
        final Mustache content = factory.compile(srcName);
        final String dstName = writeString(dstNameTemplate);
        write(content, root.resolveFile(dstName), force);
    }

    private void write(Mustache mustache, FileObject dest, boolean force) throws FileSystemException {
        if(dest.exists() && !force) {
            return;
        }
        dest.createFile();
        try(final PrintWriter writer = new PrintWriter(dest.getContent().getOutputStream())) {
            mustache.execute(writer, objects);
        }
    }

    public String writeString(String template) {
        final Mustache dest = factory.compile(new StringReader(template), "nameOf(" + template + ")");
        return dest.execute(new StringWriter(), objects).toString();
    }
}
