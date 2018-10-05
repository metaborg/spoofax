package org.metaborg.spoofax.meta.core.pluto.build;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.MetaborgException;
import org.metaborg.core.MetaborgRuntimeException;
import org.metaborg.core.action.EndNamedGoal;
import org.metaborg.core.build.BuildInput;
import org.metaborg.core.build.BuildInputBuilder;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.spoofax.core.build.ISpoofaxBuildOutput;
import org.metaborg.spoofax.core.build.SpoofaxCommonPaths;
import org.metaborg.spoofax.core.unit.ISpoofaxTransformUnit;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxBuilder;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxBuilderFactory;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxBuilderFactoryFactory;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxContext;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxInput;
import org.slf4j.helpers.MessageFormatter;
import org.spoofax.interpreter.core.Tools;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.sugarj.common.FileCommands;

import com.google.common.collect.Iterables;

import build.pluto.BuildUnit.State;
import build.pluto.dependency.Origin;
import io.usethesource.capsule.BinaryRelation;

public class StrIncrFrontEnd extends SpoofaxBuilder<StrIncrFrontEnd.Input, StrIncrFrontEnd.Output> {
    private static final String COMPILE_GOAL_NAME = "Compile";
    private static final String STRATEGO_LANG_NAME = "StrategoSugar";

    public static class Input extends SpoofaxInput {
        private static final long serialVersionUID = 1548589152421064400L;

        public final SpoofaxCommonPaths paths;
        public final File inputFile;
        public final Origin origin;

        public Input(SpoofaxContext context, SpoofaxCommonPaths paths, File inputFile, Origin origin) {
            super(context);

            this.paths = paths;
            this.inputFile = inputFile;
            this.origin = origin;
        }
    }

    public static class Output implements build.pluto.output.Output {
        private static final long serialVersionUID = 3808911543715367986L;

        public final BuildRequest request;
        public final String moduleName;
        public final BinaryRelation.Immutable<String, File> generatedFiles;

        public Output(BuildRequest request, String moduleName, BinaryRelation.Immutable<String, File> generatedFiles) {
            this.request = request;
            this.moduleName = moduleName;
            this.generatedFiles = generatedFiles;
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

        IStrategoTerm result = callStrategoCompileBuilder(context.resourceService().resolve(input.inputFile));

        String moduleName = Tools.javaStringAt(result, 0);
        IStrategoList strategyList = Tools.listAt(result, 1);
        BinaryRelation.Transient<String, File> generatedFiles = BinaryRelation.Transient.of();

        for(IStrategoTerm strategyTerm : strategyList) {
            String strategy = Tools.asJavaString(strategyTerm);
            File file = context.toFile(input.paths.strSepCompStrategyFile(moduleName, strategy));
            generatedFiles.__insert(strategy, file);
            provide(file);
        }

        provide(context.toFile(input.paths.strSepCompBoilerplateFile(moduleName)));

        setState(State.finished(true));
        return new Output(request(input), moduleName, generatedFiles.freeze());
    }

    @Override protected String description(Input input) {
        return "Compile Stratego to separate strategy ast files";
    }

    @Override public File persistentPath(Input input) {
        final Path rel = FileCommands.getRelativePath(context.baseDir, input.inputFile);
        final String relname = rel.toString().replace(File.separatorChar, '_');
        return context.depPath("str_sep_front." + relname + ".dep");
    }

    public IStrategoTerm callStrategoCompileBuilder(FileObject resource) throws IOException {
        final BuildInputBuilder inputBuilder = new BuildInputBuilder(context.project());
        ILanguageImpl strategoLangImpl = context.languageService().getLanguage(STRATEGO_LANG_NAME).activeImpl();
        inputBuilder.addLanguage(strategoLangImpl).withDefaultIncludePaths(false).addSource(resource)
            .withAnalysis(false).addTransformGoal(new EndNamedGoal(COMPILE_GOAL_NAME));

        BuildInput input;
        try {
            input = inputBuilder.build(context.dependencyService(), context.languagePathService());
        } catch(MetaborgException e) {
            throw new IOException(e);
        }

        final ISpoofaxTransformUnit<?> result;
        try {
            final ISpoofaxBuildOutput output = context.runner().build(input, null, null).schedule().block().result();
            if(!output.success()) {
                throw new IOException("Transformation failed");
            } else {
                final Iterable<ISpoofaxTransformUnit<?>> results = output.transformResults();
                final int resultSize = Iterables.size(results);
                if(resultSize == 1) {
                    result = Iterables.get(results, 0);
                } else {
                    final String message = MessageFormatter
                        .arrayFormat("{} transform results were returned instead of 1", new Object[] { resultSize })
                        .getMessage();
                    throw new IOException(message);
                }
            }
        } catch(MetaborgRuntimeException e) {
            throw new IOException("Transformation failed", e);
        } catch(InterruptedException e) {
            throw new IOException("Transformation was cancelled", e);
        }

        return result.ast();
    }
}
