package org.metaborg.spoofax.meta.core.build;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import org.apache.commons.io.FileUtils;
import org.apache.commons.vfs2.AllFileSelector;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.metaborg.core.MetaborgException;
import org.metaborg.core.action.CompileGoal;
import org.metaborg.core.action.EndNamedGoal;
import org.metaborg.core.build.BuildInput;
import org.metaborg.core.build.BuildInputBuilder;
import org.metaborg.core.build.dependency.IDependencyService;
import org.metaborg.core.build.paths.ILanguagePathService;
import org.metaborg.core.config.ILanguageComponentConfig;
import org.metaborg.core.config.ILanguageComponentConfigBuilder;
import org.metaborg.core.config.ILanguageComponentConfigWriter;
import org.metaborg.core.messages.StreamMessagePrinter;
import org.metaborg.core.resource.IResourceService;
import org.metaborg.core.source.ISourceTextService;
import org.metaborg.spoofax.core.SpoofaxConstants;
import org.metaborg.spoofax.core.processing.ISpoofaxProcessorRunner;
import org.metaborg.spoofax.meta.core.config.ISpoofaxLanguageSpecConfig;
import org.metaborg.spoofax.meta.core.config.ISpoofaxLanguageSpecConfigWriter;
import org.metaborg.spoofax.meta.core.config.LanguageSpecBuildPhase;
import org.metaborg.spoofax.meta.core.config.SdfVersion;
import org.metaborg.spoofax.meta.core.config.StrategoFormat;
import org.metaborg.spoofax.meta.core.generator.GeneratorSettings;
import org.metaborg.spoofax.meta.core.generator.language.ContinuousLanguageSpecGenerator;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxContext;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxReporting;
import org.metaborg.spoofax.meta.core.pluto.build.main.GenerateSourcesBuilder;
import org.metaborg.spoofax.meta.core.pluto.build.main.PackageBuilder;
import org.metaborg.spoofax.meta.core.project.ISpoofaxLanguageSpec;
import org.metaborg.spoofax.meta.core.project.ISpoofaxLanguageSpecPaths;
import org.metaborg.util.cmd.Arguments;
import org.metaborg.util.file.FileAccess;
import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.LoggerUtils;
import org.metaborg.util.resource.FileSelectorUtils;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Injector;

import build.pluto.builder.BuildManagers;
import build.pluto.builder.BuildRequest;
import build.pluto.builder.RequiredBuilderFailed;
import build.pluto.dependency.Origin;
import build.pluto.output.Output;
import build.pluto.xattr.Xattr;

public class LanguageSpecBuilder {
    private static final ILogger logger = LoggerUtils.logger(LanguageSpecBuilder.class);
    private static final String failingRebuildMessage =
        "Previous build failed and no change in the build input has been observed, not rebuilding. Fix the problem, or clean and rebuild the project to force a rebuild";

    private final Injector injector;
    private final IResourceService resourceService;
    private final ISourceTextService sourceTextService;
    private final IDependencyService dependencyService;
    private final ILanguagePathService languagePathService;
    private final ISpoofaxProcessorRunner runner;
    private final Set<IBuildStep> buildSteps;
    private final ILanguageComponentConfigBuilder componentConfigBuilder;
    private final ILanguageComponentConfigWriter componentConfigWriter;
    private final ISpoofaxLanguageSpecConfigWriter languageSpecConfigWriter;


    @Inject public LanguageSpecBuilder(Injector injector, IResourceService resourceService,
        ISourceTextService sourceTextService, IDependencyService dependencyService,
        ILanguagePathService languagePathService, ISpoofaxProcessorRunner runner, Set<IBuildStep> buildSteps,
        ILanguageComponentConfigBuilder componentConfigBuilder, ILanguageComponentConfigWriter componentConfigWriter,
        ISpoofaxLanguageSpecConfigWriter languageSpecConfigWriter) {
        this.injector = injector;
        this.resourceService = resourceService;
        this.sourceTextService = sourceTextService;
        this.dependencyService = dependencyService;
        this.languagePathService = languagePathService;
        this.runner = runner;
        this.componentConfigBuilder = componentConfigBuilder;
        this.componentConfigWriter = componentConfigWriter;
        this.languageSpecConfigWriter = languageSpecConfigWriter;
        this.buildSteps = buildSteps;
    }


    public void initialize(LanguageSpecBuildInput input) throws MetaborgException {
        final ISpoofaxLanguageSpecPaths paths = input.languageSpec().paths();
        try {
            paths.includeFolder().createFolder();
            paths.libFolder().createFolder();
            paths.generatedSourceFolder().createFolder();
            paths.generatedSyntaxFolder().createFolder();
        } catch(FileSystemException e) {
            throw new MetaborgException("Initializing directories failed", e);
        }

        for(IBuildStep buildStep : buildSteps) {
            buildStep.execute(LanguageSpecBuildPhase.initialize, input);
        }
    }

    public void generateSources(LanguageSpecBuildInput input, @Nullable FileAccess access) throws Exception {
        final FileObject location = input.languageSpec().location();
        logger.debug("Generating sources for {}", input.languageSpec().location());

        final ContinuousLanguageSpecGenerator generator = new ContinuousLanguageSpecGenerator(
            new GeneratorSettings(input.languageSpec().config(), input.languageSpec().paths()), access);
        generator.generateAll();

        componentConfigBuilder.reset();
        componentConfigBuilder.copyFrom(input.languageSpec().config());
        final ILanguageComponentConfig config = componentConfigBuilder.build(location);
        componentConfigWriter.write(location, config, access);

        // FIXME: This is temporary, until we've moved to the new config system completely.
        // As there's then no need to write the config file.
        if(!this.languageSpecConfigWriter.exists(input.languageSpec())) {
            this.languageSpecConfigWriter.write(input.languageSpec(), input.languageSpec().config(), access);
        }

        for(IBuildStep buildStep : buildSteps) {
            buildStep.execute(LanguageSpecBuildPhase.generateSources, input);
        }
    }

    public void compilePreJava(LanguageSpecBuildInput input) throws MetaborgException {
        logger.debug("Running pre-Java build for {}", input.languageSpec().location());

        initPluto();
        try {
            plutoBuild(GenerateSourcesBuilder.request(generateSourcesBuilderInput(input)));
        } catch(RequiredBuilderFailed e) {
            if(e.getMessage().contains("no rebuild of failing builder")) {
                throw new MetaborgException(failingRebuildMessage, e);
            } else {
                throw new MetaborgException("Rebuilding failed.", e);
            }
        } catch(RuntimeException e) {
            throw e;
        } catch(Throwable e) {
            throw new MetaborgException(e);
        }


        // HACK: compile the main ESV file to make sure that packed.esv file is always available.
        final FileObject mainEsvFile = input.languageSpec().paths().mainEsvFile();
        try {
            if(mainEsvFile.exists()) {
                logger.info("Compiling ESV file {}", mainEsvFile);
                // @formatter:off
                final BuildInput buildInput = 
                    new BuildInputBuilder(input.languageSpec())
                    .addSource(mainEsvFile)
                    .addTransformGoal(new CompileGoal())
                    .withMessagePrinter(new StreamMessagePrinter(sourceTextService, false, true, logger))
                    .build(dependencyService, languagePathService);
                // @formatter:on
                runner.build(buildInput, null, null).schedule().block();
            }
        } catch(FileSystemException e) {
            final String message = logger.format("Could not compile ESV file {}", mainEsvFile);
            throw new MetaborgException(message, e);
        } catch(InterruptedException e) {
            // Ignore
        }

        // HACK: compile the main DS file if available, after generating sources (because ds can depend on Stratego
        // strategies), to generate an interpreter.
        final FileObject mainDsFile = input.languageSpec().paths().dsMainFile();
        try {
            if(mainDsFile.exists()) {
                logger.info("Compiling DynSem file {}", mainDsFile);
                // @formatter:off
                final BuildInput buildInput =
                    new BuildInputBuilder(input.languageSpec())
                    .addSource(mainDsFile)
                    .addTransformGoal(new EndNamedGoal("All to Java"))
                    .withMessagePrinter(new StreamMessagePrinter(sourceTextService, false, true, logger))
                    .build(dependencyService, languagePathService);
                // @formatter:on
                runner.build(buildInput, null, null).schedule().block();
            }
        } catch(FileSystemException e) {
            final String message = logger.format("Could not compile DynSem file {}", mainDsFile);
            throw new MetaborgException(message, e);
        } catch(InterruptedException e) {
            // Ignore
        }

        for(IBuildStep buildStep : buildSteps) {
            buildStep.execute(LanguageSpecBuildPhase.preJava, input);
        }
    }

    public void compilePostJava(LanguageSpecBuildInput input) throws MetaborgException {
        logger.debug("Running post-Java build for {}", input.languageSpec().location());

        initPluto();
        try {
            final Origin origin = GenerateSourcesBuilder.origin(generateSourcesBuilderInput(input));
            plutoBuild(PackageBuilder.request(packageBuilderInput(input, origin)));
        } catch(RequiredBuilderFailed e) {
            if(e.getMessage().contains("no rebuild of failing builder")) {
                throw new MetaborgException(failingRebuildMessage);
            }
            throw new MetaborgException();
        } catch(RuntimeException e) {
            throw e;
        } catch(Throwable e) {
            throw new MetaborgException(e);
        }

        for(IBuildStep buildStep : buildSteps) {
            buildStep.execute(LanguageSpecBuildPhase.postJava, input);
        }
    }

    public void clean(LanguageSpecBuildInput input) throws MetaborgException {
        logger.debug("Cleaning {}", input.languageSpec().location());

        final ISpoofaxLanguageSpecPaths paths = input.languageSpec().paths();
        final AllFileSelector selector = new AllFileSelector();
        try {
            paths.strJavaTransFolder().delete(selector);
            paths.includeFolder().delete(selector);
            paths.generatedSourceFolder().delete(selector);
            paths.cacheFolder().delete(selector);
            paths.dsGeneratedInterpreterJava().delete(FileSelectorUtils.extension("java"));
        } catch(FileSystemException e) {
            throw new MetaborgException("Cleaning directories failed", e);
        }

        try {
            Xattr.getDefault().clear();
        } catch(IOException e) {
            throw new MetaborgException("Cleaning Pluto file attributes failed", e);
        }

        for(IBuildStep buildStep : buildSteps) {
            buildStep.execute(LanguageSpecBuildPhase.clean, input);
        }
    }


    private void initPluto() {
        SpoofaxContext.init(injector);
    }

    private <Out extends Output> Out plutoBuild(BuildRequest<?, Out, ?, ?> buildRequest) throws Throwable {
        return BuildManagers.build(buildRequest, new SpoofaxReporting());
    }

    private GenerateSourcesBuilder.Input generateSourcesBuilderInput(LanguageSpecBuildInput input)
        throws FileSystemException, MetaborgException {
        final ISpoofaxLanguageSpec languageSpec = input.languageSpec();
        final ISpoofaxLanguageSpecConfig config = languageSpec.config();
        final FileObject baseLoc = input.languageSpec().location();
        final FileObject buildInfoLoc = baseLoc.resolveFile("target").resolveFile("pluto");
        final SpoofaxContext context = new SpoofaxContext(baseLoc, buildInfoLoc);

        final File baseDir = context.baseDir;

        // SDF
        final String sdfModule = config.sdfName();
        final File sdfFileCandidate;
        final SdfVersion sdfVersion = config.sdfVersion();
        switch(sdfVersion) {
            case sdf2:
                sdfFileCandidate = FileUtils.getFile(baseDir, "syntax", sdfModule + ".sdf");
                break;
            case sdf3:
                sdfFileCandidate = FileUtils.getFile(baseDir, "src-gen", "syntax", sdfModule + ".sdf");
                break;
            default:
                throw new MetaborgException("Unknown SDF version: " + sdfVersion);
        }
        final @Nullable File sdfFile;
        if(sdfFileCandidate.exists()) {
            sdfFile = sdfFileCandidate;
        } else {
            sdfFile = null;
        }
        final @Nullable File sdfExternalDef;
        if(config.sdfExternalDef() != null) {
            sdfExternalDef = new File(config.sdfExternalDef());
        } else {
            sdfExternalDef = null;
        }
        final Iterable<FileObject> sdfIncludePaths =
            languagePathService.sourceAndIncludePaths(languageSpec, SpoofaxConstants.LANG_SDF_NAME);
        final List<File> packSdfIncludePaths = Lists.newArrayList();
        for(FileObject path : sdfIncludePaths) {
            final File localPath = resourceService.localFile(path);
            packSdfIncludePaths.add(localPath);
        }
        final Arguments packSdfArgs = config.sdfArgs();

        // Stratego
        final String strModule = config.strategoName();
        final File strFileCandidate = FileUtils.getFile(baseDir, "trans", strModule + ".str");
        final @Nullable File strFile;
        if(strFileCandidate.exists()) {
            strFile = strFileCandidate;
        } else {
            strFile = null;
        }
        final String strJavaStratPackage = config.identifier().id + ".strategies";
        final String strJavaStratPackagePath = strJavaStratPackage.replace('.', '/');
        final File strJavaStratFileCandidate =
            FileUtils.getFile(baseDir, "src", "main", "strategies", strJavaStratPackagePath, "Main.java");
        final @Nullable File strJavaStratFile;
        if(strJavaStratFileCandidate.exists()) {
            strJavaStratFile = strJavaStratFileCandidate;
        } else {
            strJavaStratFile = null;
        }
        final StrategoFormat strFormat = config.strFormat();
        final @Nullable File strExternalJar;
        if(config.strExternalJar() != null) {
            strExternalJar = new File(config.strExternalJar());
        } else {
            strExternalJar = null;
        }
        final String strExternalJarFlags = config.strExternalJarFlags();
        final Iterable<FileObject> strIncludePaths =
            languagePathService.sourceAndIncludePaths(languageSpec, SpoofaxConstants.LANG_STRATEGO_NAME);
        final List<File> strjIncludeDirs = Lists.newArrayList();
        for(FileObject path : strIncludePaths) {
            final File localPath = resourceService.localFile(path);
            strjIncludeDirs.add(localPath);
        }
        final Arguments strjArgs = config.strArgs();

        return new GenerateSourcesBuilder.Input(context, sdfModule, sdfFile, sdfVersion, sdfExternalDef,
            packSdfIncludePaths, packSdfArgs, null, null, strFile, strJavaStratPackage, strJavaStratFile, strFormat,
            strExternalJar, strExternalJarFlags, strjIncludeDirs, strjArgs);
    }

    private PackageBuilder.Input packageBuilderInput(LanguageSpecBuildInput input, Origin generateSourcesOrigin)
        throws FileSystemException {
        final ISpoofaxLanguageSpec languageSpec = input.languageSpec();
        final ISpoofaxLanguageSpecConfig config = languageSpec.config();
        final FileObject baseLoc = input.languageSpec().location();
        final FileObject buildInfoLoc = baseLoc.resolveFile("target").resolveFile("pluto");
        final SpoofaxContext context = new SpoofaxContext(baseLoc, buildInfoLoc);

        final File baseDir = context.baseDir;

        final StrategoFormat strFormat = config.strFormat();
        // TODO: extract method
        final String strJavaStratPackage = config.identifier().id + ".strategies";
        final String strJavaStratPackagePath = strJavaStratPackage.replace('.', '/');
        final File strJavaStratFileCandidate =
            FileUtils.getFile(baseDir, "src", "main", "strategies", strJavaStratPackagePath, "Main.java");
        final @Nullable File strJavaStratFile;
        if(strJavaStratFileCandidate.exists()) {
            strJavaStratFile = strJavaStratFileCandidate;
        } else {
            strJavaStratFile = null;
        }
        final File classesDir = FileUtils.getFile(baseDir, "target", "classes");
        final File javaStratClassesDir = FileUtils.getFile(classesDir, strJavaStratPackagePath);
        final File dsGeneratedClassesDir = FileUtils.getFile(classesDir, "ds", "generated", "interpreter");
        final File dsManualClassesDir = FileUtils.getFile(classesDir, "ds", "manual", "interpreter");
        final List<File> strJavaStratIncludeDirs =
            Lists.newArrayList(javaStratClassesDir, dsGeneratedClassesDir, dsManualClassesDir);

        return new PackageBuilder.Input(context, generateSourcesOrigin, strFormat, strJavaStratFile,
            strJavaStratIncludeDirs);
    }
}
