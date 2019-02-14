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
import java.util.Iterator;
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

        @Override public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((inputFile == null) ? 0 : inputFile.hashCode());
            result = prime * result + ((origin == null) ? 0 : origin.hashCode());
            result = prime * result + ((projectName == null) ? 0 : projectName.hashCode());
            return result;
        }

        @Override public boolean equals(Object obj) {
            if(this == obj)
                return true;
            if(obj == null)
                return false;
            if(getClass() != obj.getClass())
                return false;
            Input other = (Input) obj;
            if(inputFile == null) {
                if(other.inputFile != null)
                    return false;
            } else if(!inputFile.equals(other.inputFile))
                return false;
            if(origin == null) {
                if(other.origin != null)
                    return false;
            } else if(!origin.equals(other.origin))
                return false;
            if(projectName == null) {
                if(other.projectName != null)
                    return false;
            } else if(!projectName.equals(other.projectName))
                return false;
            return true;
        }
    }

    public static class Output implements build.pluto.output.Output {
        private static final long serialVersionUID = 3808911543715367986L;

        public final String moduleName;
        public final Map<String, File> strategyFiles;
        public final Map<String, Set<String>> strategyConstrFiles;
        public final Map<String, File> overlayFiles;
        public final List<Import> imports;



        public Output(String moduleName, Map<String, File> strategyFiles, Map<String, Set<String>> strategyConstrFiles, Map<String, File> overlayFiles, List<Import> imports) {
            this.moduleName = moduleName;
            this.strategyFiles = strategyFiles;
            this.strategyConstrFiles = strategyConstrFiles;
            this.overlayFiles = overlayFiles;
            this.imports = imports;
        }

        @Override public String toString() {
            return "StrIncrFrontEnd$Output(" + moduleName + ", { ... }, [ ... ])";
        }

        @Override public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((strategyFiles == null) ? 0 : strategyFiles.hashCode());
            result = prime * result + ((strategyConstrFiles == null) ? 0 : strategyConstrFiles.hashCode());
            result = prime * result + ((overlayFiles == null) ? 0 : overlayFiles.hashCode());
            result = prime * result + ((imports == null) ? 0 : imports.hashCode());
            result = prime * result + ((moduleName == null) ? 0 : moduleName.hashCode());
            return result;
        }

        @Override public boolean equals(Object obj) {
            if(this == obj)
                return true;
            if(obj == null)
                return false;
            if(getClass() != obj.getClass())
                return false;
            Output other = (Output) obj;
            if(strategyFiles == null) {
                if(other.strategyFiles != null)
                    return false;
            } else if(!strategyFiles.equals(other.strategyFiles))
                return false;
            if(strategyConstrFiles == null) {
                if(other.strategyConstrFiles != null)
                    return false;
            } else if(!strategyConstrFiles.equals(other.strategyConstrFiles))
                return false;
            if(overlayFiles == null) {
                if(other.overlayFiles != null)
                    return false;
            } else if(!overlayFiles.equals(other.overlayFiles))
                return false;
            if(imports == null) {
                if(other.imports != null)
                    return false;
            } else if(!imports.equals(other.imports))
                return false;
            if(moduleName == null) {
                if(other.moduleName != null)
                    return false;
            } else if(!moduleName.equals(other.moduleName))
                return false;
            return true;
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
                        final Path strPath = dir.toPath().resolve(importString + ".str");
                        final Path rtreePath = dir.toPath().resolve(importString + ".rtree");
                        if(Files.exists(rtreePath)) {
                            result.add(rtreePath.toFile());
                        } else if(Files.exists(strPath)) {
                            result.add(strPath.toFile());
                        }
                        break;
                    }
                    case wildcard: {
                        final Path path = dir.toPath().resolve(importString);
                        if(Files.exists(path)) {
                            final File[] strFiles =
                                path.toFile().listFiles((FilenameFilter) new SuffixFileFilter(Arrays.asList(".str", ".rtree")));
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

        final FileObject resource = context.resourceService().resolve(input.inputFile);
        final IStrategoTerm result = runStrategoCompileBuilder(resource, input.projectName);

        final String moduleName = Tools.javaStringAt(result, 0);
        final IStrategoList strategyList = Tools.listAt(result, 1);
        // cifiedStratNameList (2)
        // usedStrategies (3)
        // ambiguousStratCalls (4)
        final IStrategoList importsTerm = Tools.listAt(result, 5);
        // defined constructors (6)
        final IStrategoList usedConstrList = Tools.listAt(result, 7);
        final IStrategoList overlayList = Tools.listAt(result, 8);
        assert strategyList.size() == usedConstrList.size() : "Inconsistent compiler: strategy list size (" + strategyList.size() + ") != used constructors list size (" + usedConstrList.size() + ")";

        final Map<String, File> strategyFiles = new HashMap<>();
        final Map<String, Set<String>> strategyConstrFiles = new HashMap<>();
        for(Iterator<IStrategoTerm> strategyIterator = strategyList.iterator(), usedConstrIterator = usedConstrList.iterator(); strategyIterator.hasNext();) {
            String strategy = Tools.asJavaString(strategyIterator.next());

            IStrategoTerm usedConstrTerms = usedConstrIterator.next();
            Set<String> usedConstrs = new HashSet<>(usedConstrTerms.getSubtermCount());
            for(IStrategoTerm usedConstrTerm : usedConstrTerms) {
                usedConstrs.add(Tools.asJavaString(usedConstrTerm));
            }
            strategyConstrFiles.put(strategy, usedConstrs);

            File file = context.toFile(paths.strSepCompStrategyFile(input.projectName, moduleName, strategy));
            provide(context.toFile(paths.strSepCompConstrListFile(input.projectName, moduleName, strategy)));
            strategyFiles.put(strategy, file);
            provide(file);
        }
        final Map<String, File> overlayFiles = new HashMap<>();
        for(IStrategoTerm overlayTerm : overlayList) {
            String overlayName = Tools.asJavaString(overlayTerm);
            File file = context.toFile(paths.strSepCompOverlayFile(input.projectName, moduleName, overlayName));
            overlayFiles.put(overlayName, file);
            provide(file);
        }

        provide(context.toFile(paths.strSepCompBoilerplateFile(input.projectName, moduleName)));

        final List<Import> imports = new ArrayList<>(importsTerm.size());
        for(IStrategoTerm importTerm : importsTerm) {
            imports.add(Import.fromTerm(importTerm));
        }

        setState(State.finished(true));
        return new Output(moduleName, strategyFiles, strategyConstrFiles, overlayFiles, imports);
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
                throw new IOException("Cannot find/load Stratego language. Please add source dependency on org.metaborg:org.metaborg.meta.lang.stratego:${metaborgVersion} in metaborg.yaml");
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
        final IStrategoTerm ast;
        final String text = context.sourceTextService().text(resource);
        if(resource.getName().getExtension() == "rtree") {
            ast = context.termFactory().parseFromString(text);
        } else { // assume extension str
            final ISpoofaxInputUnit inputUnit =
                context.unitService().inputUnit(resource, text, strategoLang, strategoDialect);
            final ISpoofaxParseUnit parseResult;
            try {
                parseResult = context.syntaxService().parse(inputUnit);
            } catch(ParseException e) {
                throw new IOException("Cannot parse stratego file " + resource, e);
            }
            if(!parseResult.valid() || !parseResult.success()) {
                throw new IOException("Cannot parse stratego file " + resource);
            }
            ast = parseResult.ast();
        }

        // TRANSFORM
        if(!context.contextService().available(strategoLang)) {
            throw new IOException("Cannot create stratego transformation context");
        }
        final IContext transformContext;
        try {
            transformContext = context.contextService().get(resource, context.project(), strategoLang);
        } catch(ContextException e) {
            throw new IOException("Cannot create stratego transformation context", e);
        }
        final ITermFactory f = context.termFactory();
        final String projectPath = transformContext.project().location().toString();
        final IStrategoTerm inputTerm = f.makeTuple(f.makeString(projectPath), f.makeString(projectName), ast);
        try {
            return context.strategoCommon().invoke(strategoLang, transformContext, inputTerm, COMPILE_STRATEGY_NAME);
        } catch(MetaborgException e) {
            throw new IOException("Transformation failed", e);
        }
    }
}
