package org.metaborg.spoofax.generator.util;

import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

public class MustacheWriter {
    
    private final File root;
    private final Object[] objs;
    private final MustacheFactory mf;

    public MustacheWriter(File root, Object[] objs, Class cls) {
        this.root = root;
        this.objs = objs;
        this.mf = new StrictMustacheFactory(new ClassResolver(cls));
    }

    public void write(String name, boolean force) throws IOException {
        write(name,name,force);
    }

    public void write(String name, String ifNameTemplate) throws IOException {
        write(name,name,ifNameTemplate);
    }

    public void write(String srcName, String dstNameTemplate, String ifNameTemplate) throws IOException {
        String ifName = writeString(ifNameTemplate);
        if ( new File(root, ifName).exists() ) {
            write(srcName, dstNameTemplate, true);
        }
    }

    public void write(String srcName, String dstNameTemplate, boolean force) throws IOException {
        Mustache content = mf.compile(srcName);
        String dstName = writeString(dstNameTemplate);
        write(content, new File(root, dstName), force);
    }

    private void write(Mustache m, File dst, boolean force) throws IOException {
        if ( dst.exists() && !force ) { return; }
        dst.getParentFile().mkdirs();
        FileWriter fw = new FileWriter(dst);
        m.execute(fw, objs);
        fw.close();
    }

    public String writeString(String template) {
        Mustache dst = mf.compile(new StringReader(template), "nameOf("+template+")");
        return dst.execute(new StringWriter(), objs).toString();
    }

}
