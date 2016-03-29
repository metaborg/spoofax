package org.metaborg.spoofax.meta.core.generator.language;

import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.metaborg.core.project.NameUtil;
import org.metaborg.core.project.ProjectException;
import org.metaborg.spoofax.meta.core.generator.BaseGenerator;
import org.metaborg.spoofax.meta.core.generator.GeneratorSettings;

/**
 * Generates language project files that are only generated once when a new language project is created. Files are not
 * specific to an IDE.
 */
public class LanguageSpecGenerator extends BaseGenerator {
    private final String[] fileExtensions;
    private final AnalysisType analysisType;


    public LanguageSpecGenerator(GeneratorSettings config) throws ProjectException {
        this(config, new String[0]);
    }

    public LanguageSpecGenerator(GeneratorSettings config, AnalysisType analysisType) throws ProjectException {
        this(config, new String[0], analysisType);
    }

    public LanguageSpecGenerator(GeneratorSettings config, String[] fileExtensions) throws ProjectException {
        this(config, fileExtensions, AnalysisType.NaBL_TS);
    }

    public LanguageSpecGenerator(GeneratorSettings config, String[] fileExtensions, AnalysisType analysisType)
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
        if(fileExtensions == null || fileExtensions.length == 0) {
            return null;
        }
        return StringUtils.join(fileExtensions, ", ");
    }

    public String fileExtension() {
        if(fileExtensions == null || fileExtensions.length == 0) {
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
        generateAllSpoofax();
        generateAllMaven();
    }

    public void generateAllSpoofax() throws IOException {
        generateConfig();
        generateGrammar();
        generateTrans();
        generateJavaStrategy();
        generateEditorServices();
        generateIgnoreFile();
    }

    public void generateConfig() throws IOException {
        writer.write("metaborg.yaml", false);
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
        writer.write("trans/completion.str", false);
    }

    public void generateJavaStrategy() throws IOException {
        String path = "src/main/strategies/{{packagePath}}/strategies/";
        writer.write(path + "InteropRegisterer.java", false);
        writer.write(path + "Main.java", false);
    }

    public void generateEditorServices() throws IOException {
        writer.write("editor/Colorer.esv", false);
        writer.writeResolve("editor/Menus.{{analysisType}}.esv", "editor/Menus.esv", false);
        writer.write("editor/Syntax.esv", false);
        writer.write("editor/Views.esv", false);
        writer.write("editor/Main.esv", false);
    }

    public void generateIgnoreFile() throws IOException {
        writer.write("vcsignore", ".gitignore", false);
    }
    
    
    public void generateAllMaven() throws IOException {
        generatePOM();
        generateDotMvn();
    }

    public void generatePOM() throws IOException {
        writer.write("pom.xml", false);
    }
    
    public void generateDotMvn() throws IOException {
        writer.write(".mvn/extensions.xml", false);
        writer.write(".mvn/jvm.config", false);
        writer.write(".mvn/maven.config", false);
        writer.write(".mvn/settings.xml", false);
    }
}
