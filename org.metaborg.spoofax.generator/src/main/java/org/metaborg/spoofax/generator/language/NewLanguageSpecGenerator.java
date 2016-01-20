package org.metaborg.spoofax.generator.language;

import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.metaborg.core.project.NameUtil;
import org.metaborg.core.project.ProjectException;
import org.metaborg.spoofax.generator.NewBaseGenerator;
import org.metaborg.spoofax.generator.project.LanguageSpecGeneratorScope;

/**
 * Generates language project files that are only generated once when a new language project is created. Files are not
 * specific to an IDE.
 */
public class NewLanguageSpecGenerator extends NewBaseGenerator {
    private final String[] fileExtensions;
    private final AnalysisType analysisType;


    public NewLanguageSpecGenerator(LanguageSpecGeneratorScope config) throws ProjectException {
        this(config, new String[0]);
    }

    public NewLanguageSpecGenerator(LanguageSpecGeneratorScope config, AnalysisType analysisType) throws ProjectException {
        this(config, new String[0], analysisType);
    }

    public NewLanguageSpecGenerator(LanguageSpecGeneratorScope config, String[] fileExtensions) throws ProjectException {
        this(config, fileExtensions, AnalysisType.NaBL_TS);
    }

    public NewLanguageSpecGenerator(LanguageSpecGeneratorScope config, String[] fileExtensions, AnalysisType analysisType)
        throws ProjectException {
        super(config);

        for(String ext : fileExtensions) {
            if(!NameUtil.isValidFileExtension(ext)) {
                throw new ProjectException("Invalid file extension: " + ext);
            }
        }
        this.fileExtensions = fileExtensions;
        this.analysisType = analysisType;
    }


    public String fileExtensions() {
        if(fileExtensions.length == 0) {
            return null;
        }
        return StringUtils.join(fileExtensions, ", ");
    }

    public String fileExtension() {
        if(fileExtensions.length == 0) {
            return null;
        }
        return fileExtensions[0];
    }

    public String startSymbol() {
        return "Start";
    }

    public String analysisType() {
        return analysisType.name;
    }

    public boolean analysisEnabled() {
        return analysisType != AnalysisType.None;
    }


    public void generateAll() throws IOException {
        generateConfig();
        generatePOM();
        generateGrammar();
        generateTrans();
        generateInclude();
        generateJavaStrategy();
        generateEditorServices();
        generateIgnoreFile();
    }

    public void generateConfig() throws IOException {
        writer.write("metaborg.yml", false);
    }

    public void generatePOM() throws IOException {
        writer.write("pom.xml", false);
    }

    public void generateGrammar() throws IOException {
        writer.write("syntax/Common.sdf3", false);
        writer.write("syntax/{{name}}.sdf3", false);
    }

    public void generateTrans() throws IOException {
        writer.write("trans/{{strategoName}}.str", false);
        if(analysisEnabled()) {
            writer.writeResolve("trans/analysis.{{analysisType}}.str", "trans/analysis.str", false);
        }
        writer.write("trans/outline.str", false);
        writer.write("trans/pp.str", false);
    }

    public void generateInclude() throws IOException {
        writer.write("include/{{strategoName}}.str", false);
        writer.write("include/{{strategoName}}-parenthesize.str", false);
    }

    public void generateJavaStrategy() throws IOException {
        String path = "editor/java/{{packagePath}}/strategies/";
        writer.write(path + "InteropRegisterer.java", false);
        writer.write(path + "java_strategy_0_0.java", false);
        writer.write(path + "Main.java", false);
    }

    public void generateEditorServices() throws IOException {
        writer.write("editor/Colorer.esv", false);
        writer.writeResolve("editor/Menus.{{analysisType}}.esv", "editor/Menus.esv", false);
        writer.write("editor/Syntax.esv", false);
        writer.write("editor/Views.esv", false);
        writer.write("editor/{{name}}.main.esv", false);
    }

    public void generateIgnoreFile() throws IOException {
        writer.write("vcsignore", ".gitignore", false);
    }
}
