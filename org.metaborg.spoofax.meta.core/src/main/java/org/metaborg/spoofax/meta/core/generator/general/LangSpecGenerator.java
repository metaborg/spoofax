package org.metaborg.spoofax.meta.core.generator.general;

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
public class LangSpecGenerator extends BaseGenerator {
    private final LangSpecGeneratorSettings config;


    public LangSpecGenerator(LangSpecGeneratorSettings config) throws ProjectException {
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

    public SyntaxType syntaxType() {
        return config.syntaxType;
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

    public boolean analysisEnabled() {
        return syntaxEnabled() && config.generatorSettings.analysisType() != AnalysisType.None;
    }

    public boolean analysisNablTs() {
        return syntaxEnabled() && config.generatorSettings.analysisType() == AnalysisType.NaBL_TS;
    }

    public boolean analysisNabl2() {
        return syntaxEnabled() && config.generatorSettings.analysisType() == AnalysisType.NaBL2;
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
        writer.write("langspec/metaborg.yaml", "metaborg.yaml", false);
    }

    public void generateGrammar() throws IOException {
        if(syntaxEnabled()) {
            switch(config.syntaxType) {
                case SDF2:
                    writer.write("langspec/syntax/Common.sdf", "syntax/Common.sdf", false);
                    writer.write("langspec/syntax/{{name}}.sdf", "syntax/{{name}}.sdf", false);
                    break;
                case SDF3:
                    writer.write("langspec/syntax/Common.sdf3", "syntax/Common.sdf3", false);
                    writer.write("langspec/syntax/{{name}}.sdf3", "syntax/{{name}}.sdf3", false);
                    break;
                case None:
                default:
                    break;
            }
        }
    }

    public void generateTrans() throws IOException {
        writer.write("langspec/trans/{{strategoName}}.str", "trans/{{strategoName}}.str", false);
        if(analysisEnabled()) {
            writer.writeResolve("langspec/trans/analysis.{{analysisType.id}}.str", "trans/analysis.str", false);
            if(analysisNabl2()) {
                writer.write("langspec/trans/static-semantics.nabl2", "trans/static-semantics.nabl2", false);
            }
        }
        if(syntaxEnabled()) {
            writer.write("langspec/trans/outline.str", "trans/outline.str", false);
            writer.writeResolve("langspec/trans/pp.{{syntaxType.id}}.str", "trans/pp.str", false);
        }
    }

    public void generateJavaStrategy() throws IOException {
        String path = "src/main/strategies/{{strategiesPackagePath}}/";
        writer.write("langspec/" + path + "InteropRegisterer.java", path + "InteropRegisterer.java", false);
        writer.write("langspec/" + path + "Main.java", path + "Main.java", false);
    }

    public void generateEditorServices() throws IOException {
        if(syntaxEnabled()) {
            writer.writeResolve("langspec/editor/Syntax.{{syntaxType.id}}.esv", "editor/Syntax.esv", false);
        }
        if(analysisEnabled()) {
            writer.writeResolve("langspec/editor/Analysis.{{analysisType.id}}.esv", "editor/Analysis.esv", false);
        }
        writer.write("langspec/editor/Main.esv", "editor/Main.esv", false);
    }

    public void generateIgnoreFile() throws IOException {
        writer.write("langspec/vcsignore", ".gitignore", false);
    }


    public void generateAllMaven() throws IOException {
        generatePOM();
        generateDotMvn();
    }

    public void generatePOM() throws IOException {
        writer.write("langspec/pom.xml", "pom.xml", false);
    }

    public void generateDotMvn() throws IOException {
        writer.write("langspec/.mvn/extensions.xml", ".mvn/extensions.xml", false);
        writer.write("langspec/.mvn/jvm.config", ".mvn/jvm.config", false);
        writer.write("langspec/.mvn/maven.config", ".mvn/maven.config", false);
        writer.write("langspec/.mvn/settings.xml", ".mvn/settings.xml", false);
    }
}
