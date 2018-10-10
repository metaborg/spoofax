package org.metaborg.spoofax.meta.core.pluto.build;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.MetaborgException;
import org.metaborg.core.context.ContextException;
import org.metaborg.core.context.IContext;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.syntax.ParseException;
import org.metaborg.spoofax.core.unit.ISpoofaxInputUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxParseUnit;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxBuilder;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxBuilderFactory;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxBuilderFactoryFactory;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxContext;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxInput;
import org.spoofax.interpreter.core.Tools;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.sugarj.common.FileCommands;

import build.pluto.BuildUnit.State;
import build.pluto.dependency.Origin;

public class StrIncrFrontEnd extends SpoofaxBuilder<StrIncrFrontEnd.Input, StrIncrFrontEnd.Output> {
    private static final String COMPILE_STRATEGY_NAME = "clean-and-compile-module";

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

        public Output(String moduleName, Map<String, File> generatedFiles) {
            this.moduleName = moduleName;
            this.generatedFiles = generatedFiles;
        }

        @Override public String toString() {
            return "StrIncrFrontEnd$Output(" + moduleName + ", { ... })";
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
        Map<String, File> generatedFiles = new HashMap<>();

        for(IStrategoTerm strategyTerm : strategyList) {
            String strategy = Tools.asJavaString(strategyTerm);
            File file = context.toFile(paths.strSepCompStrategyFile(input.projectName, moduleName, strategy));
            generatedFiles.put(strategy, file);
            provide(file);
        }

        provide(context.toFile(paths.strSepCompBoilerplateFile(input.projectName, moduleName)));

        setState(State.finished(true));
        return new Output(moduleName, generatedFiles);
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
        final ILanguageImpl strategoLangImpl = context.languageIdentifierService().identify(resource);
        if(strategoLangImpl == null) {
            throw new IOException("Cannot find/load Stratego language, unable to build...");
        }

        // PARSE
        final String text = context.sourceTextService().text(resource);
        final ISpoofaxInputUnit inputUnit = context.unitService().inputUnit(resource, text, strategoLangImpl, null);
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
        if(!context.contextService().available(strategoLangImpl)) {
            throw new IOException("Cannot create stratego transformation context");
        }
        IContext transformContext;
        try {
            transformContext = context.contextService().get(resource, context.project(), strategoLangImpl);
        } catch(ContextException e) {
            throw new IOException("Cannot create stratego transformation context", e);
        }
        ITermFactory f = context.termFactory();
        String projectPath = transformContext.project().location().toString();
        IStrategoTerm inputTerm = f.makeTuple(f.makeString(projectPath), f.makeString(projectName), parseResult.ast());
        try {
            return context.strategoCommon().invoke(strategoLangImpl, transformContext, inputTerm , COMPILE_STRATEGY_NAME);
        } catch(MetaborgException e) {
            throw new IOException("Transformation failed", e);
        }
    }
}
