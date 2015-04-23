package org.metaborg.spoofax.generator;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Arrays;

public class Generator {

    private final File projectDir;
    private final Options o;
    private final MustacheFactory mf;

    public Generator(File projectDir, Options o) {
        this.projectDir = projectDir;
        this.o = o;
        this.mf = new DefaultMustacheFactory(new ClassResolver(Generator.class));
    }

    public void generateAll() throws IOException {
        generateGrammar();
        generateTrans();
        generateJavaStrategy();
        generateNabl();
        generateTest();
        generateCommonTrans();
        generateRuntimeLibrary();
        generateIgnoreFiles();
    }

    public void generateGrammar() throws IOException {
        write("syntax/Common.sdf3", null, o.resetFiles);
        write("syntax/{{sdfMainModule}}.sdf3", "syntax/{{sdfMainModule}}.min.sdf3", o.resetFiles);
    }

    public void generateTrans() throws IOException {
        write("trans/{{transModuleName}}.str", null, false);
        write("trans/generate.str", "trans/generate.min.str", false);
        write("trans/check.str", "trans/check.min.str", false);
        write("trans/pp.str", "trans/pp.min.str", false);
    }

    public void generateJavaStrategy() throws IOException {
        String path = "editor/java/{{packagePath}}strategies/";
        write(path+"InteropRegisterer.java", null, false);
        write(path+"java_strategy_0_0.java", null, false);
        write(path+"Main.java", null, false);
    }

    public void generateNabl() throws IOException {
        write("trans/names.nab", "trans/names.min.nab", false);
    }

    public void generateTest() throws IOException {
        if ( o.generateMinimal ) { return; }
        write("test/example.{{editorExtension}}", null, false);
        write("test/test-example.spt", null, false);
    }

    public void generateCommonTrans() throws IOException {
        write("lib/editor-common.generated.str", null, true);
        write("lib/refactor-common.generated.str", null, true);
    }

    public void generateRuntimeLibrary() throws IOException {
        write("lib/libruntime.rtree", null, true);
    }

    public void generateIgnoreFiles() throws IOException {
        if ( !o.generateVCIgnores ) { return; }
        write(".gitignore", null, true);
    }

    public void generatePOM() throws IOException {
        write("pom.xml", null, false);
    }

    private void write(String nameTemplate, String minimalName, boolean force) throws IOException {
        Mustache content = mf.compile(o.generateMinimal && minimalName != null ?
                minimalName : nameTemplate);
        Mustache name = mf.compile(new StringReader(nameTemplate), "nameOf("+nameTemplate+")");
        String finalName = name.execute(new StringWriter(), o).toString();
        write(content, new File(projectDir, finalName), force);
    }

    private void write(Mustache m, File dst, boolean force) throws IOException {
        if ( dst.exists() && !force ) { return; }
        dst.getParentFile().mkdirs();
        FileWriter fw = new FileWriter(dst);
        m.execute(fw, o);
        fw.close();
    }

    public static void main(String[] args) throws IOException {
        Options o = new Options("Entity");
        o.editorExtensions = Arrays.asList("ent");
        o.generateVCIgnores = true;
        Generator g = new Generator(new File("entity"), o);
        g.generateAll();
    }

}
