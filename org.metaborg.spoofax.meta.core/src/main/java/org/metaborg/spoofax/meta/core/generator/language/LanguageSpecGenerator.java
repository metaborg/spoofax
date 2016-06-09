package org.metaborg.spoofax.meta.core.generator.language;

import java.io.IOException;
import java.util.Collection;

import org.metaborg.core.project.ProjectException;
import org.metaborg.spoofax.meta.core.generator.BaseGenerator;

import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;

/**
 * Generates language project files that are only generated once when a new language project is created. Files are not
 * specific to an IDE.
 */
public class LanguageSpecGenerator extends BaseGenerator {
    private final LanguageSpecGeneratorSettings config;


    public LanguageSpecGenerator(LanguageSpecGeneratorSettings config) throws ProjectException {
        super(config.generatorSettings);

        this.config = config;
    }

    
    public String fileExtensions() {
        final Collection<String> extensions = config.extensions;
        if(extensions == null || extensions.isEmpty()) {
            return null;
        }
        return Joiner.on(", ").join(extensions);
    }

    public String fileExtension() {
        final Collection<String> extensions = config.extensions;
        if(extensions == null || extensions.isEmpty()) {
            return null;
        }
        return Iterables.get(extensions, 0);
    }

    public String syntaxType() {
        return config.syntaxType.id;
    }

    public boolean syntaxEnabled() {
        return config.syntaxType != SyntaxType.None;
    }

    public boolean syntaxSdf2() {
        return config.syntaxType == SyntaxType.SDF2;
    }

    public boolean syntaxSdf3() {
        return config.syntaxType == SyntaxType.SDF3;
    }

    public String startSymbol() {
        return "Start";
    }

    public String signaturesModule() {
        switch(config.syntaxType) {
            case None:
                return null;
            case SDF2:
                return "signatures/" + config.generatorSettings.name();
            case SDF3:
                return "signatures/" + config.generatorSettings.name() + "-sig";
        }
        return null;
    }

    public String analysisType() {
        return config.analysisType.id;
    }

    public boolean analysisEnabled() {
        return syntaxEnabled() && config.analysisType != AnalysisType.None;
    }

    public boolean analysisNablTs() {
        return syntaxEnabled() && config.analysisType == AnalysisType.NaBL_TS;
    }

    public boolean analysisNabl2() {
        return syntaxEnabled() && config.analysisType == AnalysisType.NaBL2;
    }

    public boolean syntaxOrAnalysisEnabled() {
        return syntaxEnabled() || analysisEnabled();
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
        if(syntaxEnabled()) {
            switch(config.syntaxType) {
                case SDF2:
                    writer.write("syntax/Common.sdf", false);
                    writer.write("syntax/{{name}}.sdf", false);
                    break;
                case SDF3:
                    writer.write("syntax/Common.sdf3", false);
                    writer.write("syntax/{{name}}.sdf3", false);
                    break;
                case None:
                default:
                    break;
            }
        }
    }

    public void generateTrans() throws IOException {
        writer.write("trans/{{strategoName}}.str", false);
        if(analysisEnabled()) {
            writer.writeResolve("trans/analysis.{{analysisType}}.str", "trans/analysis.str", false);
            if(analysisNabl2()) {
                writer.write("trans/static-semantics.nabl2", false);
            }
        }
        if(syntaxEnabled()) {
            writer.write("trans/outline.str", false);
            writer.writeResolve("trans/pp.{{syntaxType}}.str", "trans/pp.str", false);
            if(syntaxSdf3()) {
                writer.write("trans/completion.str", false);
            }
        }
    }

    public void generateJavaStrategy() throws IOException {
        String path = "src/main/strategies/{{strategiesPackagePath}}/";
        writer.write(path + "InteropRegisterer.java", false);
        writer.write(path + "Main.java", false);
    }

    public void generateEditorServices() throws IOException {
        if(syntaxEnabled()) {
            writer.writeResolve("editor/Syntax.{{syntaxType}}.esv", "editor/Syntax.esv", false);
        }
        if(analysisEnabled()) {
            writer.writeResolve("editor/Analysis.{{analysisType}}.esv", "editor/Analysis.esv", false);
        }
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
