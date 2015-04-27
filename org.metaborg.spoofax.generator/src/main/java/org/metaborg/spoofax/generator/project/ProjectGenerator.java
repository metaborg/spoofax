package org.metaborg.spoofax.generator.project;

import java.io.File;
import java.io.IOException;
import org.apache.commons.lang3.StringUtils;
import org.metaborg.spoofax.generator.BaseGenerator;

public class ProjectGenerator extends BaseGenerator {
    
    private final String[] editorExtensions;
    private boolean minimal = false;
    private String startSymbol = "Start";
    private String format;
    private String pkg;

    public ProjectGenerator(File root, String sdfMainModule, String[] editorExtensions) {
        super(root, sdfMainModule);
        this.editorExtensions = editorExtensions;
    }

    public boolean minimal() {
        return minimal;
    }

    public void setMinimal(boolean minimal) {
        this.minimal = minimal;
    }

    public String startSymbol() {
        return startSymbol;
    }

    public void setStartSymbol(String startSymbol) {
        this.startSymbol = startSymbol;
    }

    public String editorExtensions() {
        return StringUtils.join(editorExtensions, " ");
    }

    public String editorExtension() {
        return editorExtensions[0];
    }

    public String[] format() {
        if ( format == null ) {
            return new String[0];
        }
        return new String[] { format };
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String packageName() {
        return pkg != null && !pkg.isEmpty() ?
                pkg : sdfMainModule().toLowerCase();
    }

    public void setPackageName(String name) {
        pkg = name;
    }

    public String packagePath() {
        return packageName().replace('.', '/');
    }

    public void generateAll() throws IOException {
        generatePOM();
        generateGrammar();
        generateTrans();
        generateJavaStrategy();
        generateNabl();
        generateTest();
        generateEditorServices();
        generateIgnoreFile();
    }

    public void generatePOM() throws IOException {
        writer.write("pom.xml", false);
    }

    public void generateGrammar() throws IOException {
        writer.write("syntax/Common.sdf3", false);
        String name = "syntax/{{sdfMainModule}}.sdf3";
        writer.write(minimal ? "syntax/{{sdfMainModule}}.min.sdf3" : name, name, false);
    }

    public void generateTrans() throws IOException {
        writer.write("trans/{{transModuleName}}.str", false);
        String generateName = "trans/generate.str";
        writer.write(minimal ? "trans/generate.min.str" : generateName, generateName, false);
        String checkName = "trans/check.str";
        writer.write(minimal ? "trans/check.min.str" : checkName, checkName, false);
        String ppName = "trans/pp.str";
        writer.write(minimal ? "trans/pp.min.str" : ppName, ppName, false);
    }

    public void generateJavaStrategy() throws IOException {
        String path = "editor/java/{{packagePath}}/strategies/";
        writer.write(path+"InteropRegisterer.java", false);
        writer.write(path+"java_strategy_0_0.java", false);
        writer.write(path+"Main.java", false);
    }

    public void generateNabl() throws IOException {
        String name = "trans/names.nab";
        writer.write(minimal ? "trans/names.min.nab" : name, name, false);
    }

    public void generateTest() throws IOException {
        if ( minimal ) { return; }
        writer.write("test/example.{{editorExtension}}", false);
        writer.write("test/test-example.spt", false);
    }

    public void generateEditorServices() throws IOException {
        writer.write("editor/{{sdfMainModule}}-Colorer.esv", false);
        writer.write("editor/{{sdfMainModule}}-Colorer.generated.esv", true);
        writer.write("editor/{{sdfMainModule}}-Completions.esv", false);
        writer.write("editor/{{sdfMainModule}}-Completions.generated.esv", true);
        writer.write("editor/{{sdfMainModule}}-Folding.esv", false);
        writer.write("editor/{{sdfMainModule}}-Folding.generated.esv", true);
        writer.write("editor/{{sdfMainModule}}-Menus.esv", false);
        writer.write("editor/{{sdfMainModule}}-Outliner.str", false);
        writer.write("editor/{{sdfMainModule}}-Outliner.generated.str", true);
        writer.write("editor/{{sdfMainModule}}-Refactorings.esv", false);
        writer.write("editor/{{sdfMainModule}}-Refactorings.generated.esv", true);
        writer.write("editor/{{sdfMainModule}}-References.esv", false);
        writer.write("editor/{{sdfMainModule}}-References.generated.esv", true);
        writer.write("editor/{{sdfMainModule}}-Syntax.esv", false);
        writer.write("editor/{{sdfMainModule}}-Syntax.generated.esv", true);
        writer.write("editor/{{sdfMainModule}}-Views.esv", false);
        writer.write("editor/{{sdfMainModule}}.main.esv", false);
    }

    public void generateIgnoreFile() throws IOException {
        writer.write("vcsignore", ".gitignore", false);
    }

}
