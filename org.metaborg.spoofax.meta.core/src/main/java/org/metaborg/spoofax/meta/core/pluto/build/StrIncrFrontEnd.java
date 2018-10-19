package org.metaborg.spoofax.meta.core.pluto.build;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.MetaborgException;
import org.metaborg.core.context.ContextException;
import org.metaborg.core.context.IContext;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.syntax.ParseException;
import org.metaborg.spoofax.core.syntax.ImploderImplementation;
import org.metaborg.spoofax.core.syntax.SyntaxFacet;
import org.metaborg.spoofax.core.unit.ISpoofaxInputUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxParseUnit;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxBuilder;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxBuilderFactory;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxBuilderFactoryFactory;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxContext;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxInput;
import org.spoofax.interpreter.core.Tools;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.sugarj.common.FileCommands;

import build.pluto.BuildUnit.State;
import build.pluto.dependency.Origin;

public class StrIncrFrontEnd extends SpoofaxBuilder<StrIncrFrontEnd.Input, StrIncrFrontEnd.Output> {
    public static class Input extends SpoofaxInput {
        private static final long serialVersionUID = 1548589152421064400L;

        public final File inputFile;
        public final String projectName;
        public final Origin origin;

        public Input(SpoofaxContext context, File inputFile, String projectName, Origin origin) {
            super(context);

            this.inputFile = inputFile;
            this.projectName = projectName;
            this.origin = origin;
        }

        @Override public String toString() {
            return "StrIncrFrontEnd$Input(" + inputFile + ")";
        }
    }

    public static class Output implements build.pluto.output.Output {
        private static final long serialVersionUID = 3808911543715367986L;

        public final String moduleName;
        public final Map<String, File> generatedFiles;
        public final List<Import> imports;

        public Output(String moduleName, Map<String, File> generatedFiles, List<Import> imports) {
            this.moduleName = moduleName;
            this.generatedFiles = generatedFiles;
            this.imports = imports;
        }

        @Override public String toString() {
            return "StrIncrFrontEnd$Output(" + moduleName + ", { ... }, [ ... ])";
        }
    }

    public static class Import implements Serializable {
        public static enum ImportType {
            normal, wildcard
        }

        private static final long serialVersionUID = 7035284070137434795L;

        public final ImportType importType;
        public final String importString;

        protected Import(ImportType importType, String importString) {
            this.importType = importType;
            this.importString = importString;
        }

        public static Import normal(String importString) {
            return new Import(ImportType.normal, importString);
        }

        public static Import wildcard(String importString) {
            return new Import(ImportType.wildcard, importString);
        }

        public Set<File> resolveImport(List<File> includeDirs) throws IOException {
            Set<File> result = new HashSet<>();
            for(File dir : includeDirs) {
                switch(importType) {
                    case normal: {
                        final Path path = dir.toPath().resolve(importString + ".str");
                        if(Files.exists(path)) {
                            result.add(path.toFile());
                        }
                        break;
                    }
                    case wildcard: {
                        final Path path = dir.toPath().resolve(importString);
                        if(Files.exists(path)) {
                            final File[] strFiles =
                                path.toFile().listFiles((FilenameFilter) new SuffixFileFilter(".str"));
                            result.addAll(Arrays.asList(strFiles));
                        }
                        break;
                    }
                    default:
                        throw new IOException("Missing case for ImportType: " + importType);
                }
            }
            return result;
        }

        public static Import fromTerm(IStrategoTerm importTerm) throws IOException {
            if(!(importTerm instanceof IStrategoAppl)) {
                throw new IOException("Import term was not a constructor: " + importTerm);
            }
            final IStrategoAppl appl = (IStrategoAppl) importTerm;
            switch(appl.getName()) {
                case "Import":
                    return normal(Tools.javaStringAt(appl, 0));
                case "ImportWildcard":
                    return wildcard(Tools.javaStringAt(appl, 0));
                default:
                    throw new IOException("Import term was not the expected Import or ImportWildcard: " + appl);
            }
        }

        @Override public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((importString == null) ? 0 : importString.hashCode());
            result = prime * result + ((importType == null) ? 0 : importType.hashCode());
            return result;
        }

        @Override public boolean equals(Object obj) {
            if(this == obj)
                return true;
            if(obj == null)
                return false;
            if(getClass() != obj.getClass())
                return false;
            Import other = (Import) obj;
            if(importString == null) {
                if(other.importString != null)
                    return false;
            } else if(!importString.equals(other.importString))
                return false;
            if(importType != other.importType)
                return false;
            return true;
        }
    }

    // Just a type alias
    public static class BuildRequest extends
        build.pluto.builder.BuildRequest<Input, Output, StrIncrFrontEnd, SpoofaxBuilderFactory<Input, Output, StrIncrFrontEnd>> {
        private static final long serialVersionUID = -1299552527869341531L;

        public BuildRequest(SpoofaxBuilderFactory<Input, Output, StrIncrFrontEnd> factory, Input input) {
            super(factory, input);
        }
    }

    private static final String COMPILE_STRATEGY_NAME = "clean-and-compile-module";

    public static SpoofaxBuilderFactory<Input, Output, StrIncrFrontEnd> factory =
        SpoofaxBuilderFactoryFactory.of(StrIncrFrontEnd.class, Input.class);

    public static BuildRequest request(Input input) {
        return new BuildRequest(factory, input);
    }

    public static Origin origin(Input input) {
        return Origin.from(request(input));
    }

    public StrIncrFrontEnd(Input input) {
        super(input);
    }

    @Override protected Output build(Input input) throws Throwable {
        requireBuild(input.origin);

        require(input.inputFile);

        FileObject resource = context.resourceService().resolve(input.inputFile);
        IStrategoTerm result = runStrategoCompileBuilder(resource, input.projectName);

        String moduleName = Tools.javaStringAt(result, 0);
        IStrategoList strategyList = Tools.listAt(result, 1);
        IStrategoList importsTerm = Tools.listAt(result, 2);

        Map<String, File> generatedFiles = new HashMap<>();
        for(IStrategoTerm strategyTerm : strategyList) {
            String strategy = Tools.asJavaString(strategyTerm);
            File file = context.toFile(paths.strSepCompStrategyFile(input.projectName, moduleName, strategy));
            generatedFiles.put(strategy, file);
            provide(file);
        }

        provide(context.toFile(paths.strSepCompBoilerplateFile(input.projectName, moduleName)));

        List<Import> imports = new ArrayList<>(importsTerm.size());
        for(IStrategoTerm importTerm : importsTerm) {
            imports.add(Import.fromTerm(importTerm));
        }

        setState(State.finished(true));
        return new Output(moduleName, generatedFiles, imports);
    }

    @Override protected String description(Input input) {
        return "Compile to separate strategy ast files: " + input.inputFile;
    }

    @Override public File persistentPath(Input input) {
        final Path rel = FileCommands.getRelativePath(context.baseDir, input.inputFile);
        final String relname = rel.toString().replace(File.separatorChar, '_');
        return context.depPath("str_sep_front." + relname + ".dep");
    }

    public IStrategoTerm runStrategoCompileBuilder(FileObject resource, String projectName) throws IOException {
        ILanguageImpl strategoDialect = context.languageIdentifierService().identify(resource);
        ILanguageImpl strategoLang = context.dialectService().getBase(strategoDialect);
        if(strategoLang == null) {
            strategoLang = strategoDialect;
            strategoDialect = null;
            if(strategoLang == null) {
                throw new IOException("Cannot find/load Stratego language, unable to build...");
            }
        }
        if(strategoDialect != null) {
            final SyntaxFacet syntaxFacet = (SyntaxFacet) strategoDialect.facet(SyntaxFacet.class);
            final String dialectName = context.dialectService().dialectName(strategoDialect);
            // Get dialect with stratego imploder setting
            final ILanguageImpl adaptedStrategoDialect = context.dialectService().update(dialectName,
                syntaxFacet.withImploderSetting(ImploderImplementation.stratego));
            // Update registered dialect back to old one.
            context.dialectService().update(dialectName, syntaxFacet);
            strategoDialect = adaptedStrategoDialect;
        }

        // PARSE
        final String text = context.sourceTextService().text(resource);
        final ISpoofaxInputUnit inputUnit =
            context.unitService().inputUnit(resource, text, strategoLang, strategoDialect);
        ISpoofaxParseUnit parseResult;
        try {
            parseResult = context.syntaxService().parse(inputUnit);
        } catch(ParseException e) {
            throw new IOException("Cannot parse stratego file " + resource, e);
        }
        if(!parseResult.valid() || !parseResult.success()) {
            throw new IOException("Cannot parse stratego file " + resource);
        }

        // TRANSFORM
        if(!context.contextService().available(strategoLang)) {
            throw new IOException("Cannot create stratego transformation context");
        }
        IContext transformContext;
        try {
            transformContext = context.contextService().get(resource, context.project(), strategoLang);
        } catch(ContextException e) {
            throw new IOException("Cannot create stratego transformation context", e);
        }
        ITermFactory f = context.termFactory();
        String projectPath = transformContext.project().location().toString();
        IStrategoTerm inputTerm = f.makeTuple(f.makeString(projectPath), f.makeString(projectName), parseResult.ast());
        try {
            return context.strategoCommon().invoke(strategoLang, transformContext, inputTerm, COMPILE_STRATEGY_NAME);
        } catch(MetaborgException e) {
            throw new IOException("Transformation failed", e);
        }
    }
}
