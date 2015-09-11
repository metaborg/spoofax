package org.metaborg.spoofax.generator;

import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.metaborg.core.project.NameUtil;
import org.metaborg.core.project.ProjectException;
import org.metaborg.spoofax.generator.project.GeneratorProjectSettings;
import org.metaborg.util.file.FileAccess;

public class NewProjectGenerator extends BaseGenerator {
    private final String[] fileExtensions;

    public NewProjectGenerator(GeneratorProjectSettings settings, String[] fileExtensions, FileAccess access)
        throws ProjectException {
        super(settings, access);

        if(fileExtensions.length < 1) {
            throw new ProjectException("At least one fileExtension is required.");
        }
        for(String ext : fileExtensions) {
            if(!NameUtil.isValidFileExtension(ext)) {
                throw new ProjectException("Invalid file extension: " + ext);
            }
        }
        this.fileExtensions = fileExtensions;
    }

    public String fileExtensions() {
        return StringUtils.join(fileExtensions, ", ");
    }

    public String fileExtension() {
        return fileExtensions[0];
    }

    // ////////////////////////////////////////////////////////////////

    private boolean minimal = false;

    public boolean minimal() {
        return minimal;
    }

    public void setMinimal(boolean minimal) {
        this.minimal = minimal;
    }

    // ////////////////////////////////////////////////////////////////

    public String startSymbol() {
        return "Start";
    }

    // ////////////////////////////////////////////////////////////////

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
        String name = "syntax/{{name}}.sdf3";
        writer.write(minimal ? "syntax/{{name}}.min.sdf3" : name, name, false);
    }

    public void generateTrans() throws IOException {
        writer.write("trans/{{strategoName}}.str", false);
        String generateName = "trans/generate.str";
        writer.write(minimal ? "trans/generate.min.str" : generateName, generateName, false);
        String checkName = "trans/check.str";
        writer.write(minimal ? "trans/check.min.str" : checkName, checkName, false);
        String ppName = "trans/pp.str";
        writer.write(minimal ? "trans/pp.min.str" : ppName, ppName, false);
    }

    public void generateJavaStrategy() throws IOException {
        String path = "editor/java/{{packagePath}}/strategies/";
        writer.write(path + "InteropRegisterer.java", false);
        writer.write(path + "java_strategy_0_0.java", false);
        writer.write(path + "Main.java", false);
    }

    public void generateNabl() throws IOException {
        String name = "trans/names.nab";
        writer.write(minimal ? "trans/names.min.nab" : name, name, false);
    }

    public void generateTest() throws IOException {
        if(minimal) {
            return;
        }
        writer.write("test/example.{{fileExtension}}", false);
        writer.write("test/test-example.spt", false);
    }

    public void generateEditorServices() throws IOException {
        writer.write("editor/{{name}}-Colorer.esv", false);
        writer.write("editor/{{name}}-Completions.esv", false);
        writer.write("editor/{{name}}-Folding.esv", false);
        writer.write("editor/{{name}}-Menus.esv", false);
        writer.write("editor/{{name}}-Outliner.str", false);
        writer.write("editor/{{name}}-Refactorings.esv", false);
        writer.write("editor/{{name}}-References.esv", false);
        writer.write("editor/{{name}}-Syntax.esv", false);
        writer.write("editor/{{name}}-Views.esv", false);
        writer.write("editor/{{name}}.main.esv", false);
    }

    public void generateIgnoreFile() throws IOException {
        writer.write("vcsignore", ".gitignore", false);
    }
}
