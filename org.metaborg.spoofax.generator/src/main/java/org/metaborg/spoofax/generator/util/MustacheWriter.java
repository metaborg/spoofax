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
    private final Object obj;
    private final MustacheFactory mf;

    public MustacheWriter(File root, Object obj, Class cls) {
        this.root = root;
        this.obj = obj;
        this.mf = new StrictMustacheFactory(new ClassResolver(cls));
    }

    public void write(String name, boolean force) throws IOException {
        write(name,name,force);
    }

    public void write(String srcName, String dstNameTemplate, boolean force) throws IOException {
        Mustache content = mf.compile(srcName);
        Mustache dst = mf.compile(new StringReader(dstNameTemplate), "nameOf("+dstNameTemplate+")");
        String dstName = dst.execute(new StringWriter(), obj).toString();
        write(content, new File(root, dstName), force);
    }

    private void write(Mustache m, File dst, boolean force) throws IOException {
        if ( dst.exists() && !force ) { return; }
        dst.getParentFile().mkdirs();
        FileWriter fw = new FileWriter(dst);
        m.execute(fw, obj);
        fw.close();
    }

}
