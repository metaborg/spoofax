package org.metaborg.spoofax.meta.core;
//
//import java.io.IOException;
//import java.net.URL;
//import java.util.Set;
//
//import javax.annotation.Nullable;
//
//import org.apache.commons.vfs2.AllFileSelector;
//import org.apache.commons.vfs2.FileObject;
//import org.apache.commons.vfs2.FileSystemException;
//import org.apache.tools.ant.BuildListener;
//import org.metaborg.core.MetaborgException;
//import org.metaborg.core.action.CompileGoal;
//import org.metaborg.core.build.BuildInput;
//import org.metaborg.core.build.BuildInputBuilder;
//import org.metaborg.core.build.dependency.IDependencyService;
//import org.metaborg.core.build.paths.ILanguagePathService;
//import org.metaborg.core.processing.ICancellationToken;
//import org.metaborg.core.project.settings.YAMLProjectSettingsSerializer;
//import org.metaborg.spoofax.core.processing.ISpoofaxProcessorRunner;
//import org.metaborg.spoofax.core.project.ISpoofaxLanguageSpecPaths;
//import org.metaborg.spoofax.core.project.configuration.ISpoofaxLanguageSpecConfigWriter;
//import org.metaborg.spoofax.core.project.settings.SpoofaxProjectSettings;
//import org.metaborg.spoofax.generator.language.ProjectGenerator;
//import org.metaborg.spoofax.generator.project.GeneratorProjectSettings;
//import org.metaborg.spoofax.meta.core.pluto.SpoofaxContext;
//import org.metaborg.spoofax.meta.core.pluto.SpoofaxInput;
//import org.metaborg.spoofax.meta.core.pluto.SpoofaxReporting;
//import org.metaborg.spoofax.meta.core.pluto.build.main.GenerateSourcesBuilder;
//import org.metaborg.spoofax.meta.core.pluto.build.main.PackageBuilder;
//import org.metaborg.util.file.FileAccess;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import build.pluto.builder.BuildManagers;
//import build.pluto.builder.BuildRequest;
//import build.pluto.output.Output;
//import build.pluto.xattr.Xattr;
//
//import com.google.inject.Inject;
//import com.google.inject.Injector;

/*
// TODO: Rename to SpoofaxLanguageSpecBuilder

public class SpoofaxMetaBuilder {
    private static final Logger log = LoggerFactory.getLogger(SpoofaxMetaBuilder.class);

    private final Injector injector;
    private final IDependencyService dependencyService;
    private final ILanguagePathService languagePathService;
    private final ISpoofaxLanguageSpecConfigWriter languageSpecConfigWriter;
    private final ISpoofaxProcessorRunner runner;
    private final Set<IBuildStep> buildSteps;


    @Inject public SpoofaxMetaBuilder(
            Injector injector,
            IDependencyService dependencyService,
            ILanguagePathService languagePathService,
            ISpoofaxLanguageSpecConfigWriter languageSpecConfigWriter,
            ISpoofaxProcessorRunner runner,
            Set<IBuildStep> buildSteps) {
        this.injector = injector;
        this.dependencyService = dependencyService;
        this.languagePathService = languagePathService;
        this.languageSpecConfigWriter = languageSpecConfigWriter;
        this.runner = runner;
        this.buildSteps = buildSteps;
    }


    public void initialize(LanguageSpecBuildInput input) throws FileSystemException {
        final ISpoofaxLanguageSpecPaths paths = input.paths;
        paths.includeFolder().createFolder();
        paths.libFolder().createFolder();
        paths.generatedSourceFolder().createFolder();
        paths.generatedSyntaxFolder().createFolder();
    }

    public void generateSources(LanguageSpecBuildInput input, FileAccess access) throws Exception {
        log.debug("Generating sources for {}", input.paths.rootFolder());

        final ProjectGenerator generator = new ProjectGenerator(new GeneratorProjectSettings(input.settings), access);
        generator.generateAll();

        this.languageSpecConfigWriter.write(input.languageSpec, input.config, access);

        // HACK: compile the main ESV file to make sure that packed.esv file is always available.
        final FileObject mainEsvFile = input.paths.mainEsvFile();
        if(mainEsvFile.exists()) {
            // @formatter:off
            final BuildInput buildInput =
                new BuildInputBuilder(input.project)
                .addSource(mainEsvFile)
                .addTransformGoal(new CompileGoal())
                .build(dependencyService, languagePathService);
            // @formatter:on
            runner.build(buildInput, null, null).schedule().block();
        }
        access.addWrite(mainEsvFile);
    }

    public void compilePreJava(LanguageSpecBuildInput input, @Nullable URL[] classpaths, @Nullable BuildListener listener,
        @Nullable ICancellationToken cancellationToken) throws MetaborgException {
        log.debug("Running pre-Java build for {}", input.paths.rootFolder());

        for(IBuildStep buildStep : buildSteps) {
            buildStep.compilePreJava(input);
        }

        // final IAntRunner runner = antRunner.create(input, classpaths, listener);
        // runner.execute("generate-sources", cancellationToken);

        initPluto();
        try {
            plutoBuild(GenerateSourcesBuilder.request(createGenerateSourcesBuilderInput(input)));
        } catch(RuntimeException e) {
            throw e;
        } catch(Throwable e) {
            throw new MetaborgException("Build failed", e);
        }
    }

    public void compilePostJava(LanguageSpecBuildInput input, @Nullable URL[] classpaths, @Nullable BuildListener listener,
        @Nullable ICancellationToken cancellationToken) throws MetaborgException {
        log.debug("Running post-Java build for {}", input.project.location());

        for(IBuildStep buildStep : buildSteps) {
            buildStep.compilePostJava(input);
        }

        initPluto();
        try {
            plutoBuild(PackageBuilder.request(new SpoofaxInput(new SpoofaxContext(input.settings))));
        } catch(RuntimeException e) {
            throw e;
        } catch(Throwable e) {
            throw new MetaborgException("Build failed", e);
        }
    }

    public void clean(LanguageSpecBuildInput input) throws IOException {
        log.debug("Cleaning {}", input.paths.rootFolder());
        final AllFileSelector selector = new AllFileSelector();
        input.paths.strJavaTransFolder().delete(selector);
        input.paths.includeFolder().delete(selector);
        input.paths.generatedSourceFolder().delete(selector);
        input.paths.cacheFolder().delete(selector);
        Xattr.getDefault().clear();
    }


    private void initPluto() {
        SpoofaxContext.init(injector);
    }

    private <Out extends Output> Out plutoBuild(BuildRequest<?, Out, ?, ?> buildRequest) throws Throwable {
        return BuildManagers.build(buildRequest, new SpoofaxReporting());
    }

    private GenerateSourcesBuilder.Input createGenerateSourcesBuilderInput(LanguageSpecBuildInput input) {
        new GenerateSourcesBuilder.Input(new SpoofaxContext(
                input.settings))
    }
}*/
