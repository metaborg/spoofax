package org.metaborg.spoofax.meta.core.build;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.AllFileSelector;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.metaborg.core.MetaborgException;
import org.metaborg.core.action.CompileGoal;
import org.metaborg.core.action.EndNamedGoal;
import org.metaborg.core.build.BuildInput;
import org.metaborg.core.build.BuildInputBuilder;
import org.metaborg.core.build.CommonPaths;
import org.metaborg.core.build.dependency.IDependencyService;
import org.metaborg.core.build.paths.ILanguagePathService;
import org.metaborg.core.config.IExportConfig;
import org.metaborg.core.config.ILanguageComponentConfig;
import org.metaborg.core.config.ILanguageComponentConfigBuilder;
import org.metaborg.core.config.ILanguageComponentConfigWriter;
import org.metaborg.core.language.LanguageIdentifier;
import org.metaborg.core.messages.StreamMessagePrinter;
import org.metaborg.core.resource.IResourceService;
import org.metaborg.core.source.ISourceTextService;
import org.metaborg.spoofax.core.SpoofaxConstants;
import org.metaborg.spoofax.core.build.ISpoofaxBuildOutput;
import org.metaborg.spoofax.core.processing.ISpoofaxProcessorRunner;
import org.metaborg.spoofax.meta.core.config.ISpoofaxLanguageSpecConfig;
import org.metaborg.spoofax.meta.core.config.LanguageSpecBuildPhase;
import org.metaborg.spoofax.meta.core.config.Sdf2tableVersion;
import org.metaborg.spoofax.meta.core.config.SdfVersion;
import org.metaborg.spoofax.meta.core.config.StrategoFormat;
import org.metaborg.spoofax.meta.core.generator.GeneratorSettings;
import org.metaborg.spoofax.meta.core.generator.general.ContinuousLanguageSpecGenerator;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxContext;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxReporting;
import org.metaborg.spoofax.meta.core.pluto.build.main.ArchiveBuilder;
import org.metaborg.spoofax.meta.core.pluto.build.main.GenerateSourcesBuilder;
import org.metaborg.spoofax.meta.core.pluto.build.main.PackageBuilder;
import org.metaborg.spoofax.meta.core.project.ISpoofaxLanguageSpec;
import org.metaborg.util.cmd.Arguments;
import org.metaborg.util.file.IFileAccess;
import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.LoggerUtils;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Injector;

import build.pluto.builder.BuildManagers;
import build.pluto.builder.BuildRequest;
import build.pluto.builder.RequiredBuilderFailed;
import build.pluto.dependency.Origin;
import build.pluto.output.Output;

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


    @Inject public LanguageSpecBuilder(Injector injector, IResourceService resourceService,
        ISourceTextService sourceTextService, IDependencyService dependencyService,
        ILanguagePathService languagePathService, ISpoofaxProcessorRunner runner, Set<IBuildStep> buildSteps,
        ILanguageComponentConfigBuilder componentConfigBuilder, ILanguageComponentConfigWriter componentConfigWriter) {
        this.injector = injector;
        this.resourceService = resourceService;
        this.sourceTextService = sourceTextService;
        this.dependencyService = dependencyService;
        this.languagePathService = languagePathService;
        this.runner = runner;
        this.componentConfigBuilder = componentConfigBuilder;
        this.componentConfigWriter = componentConfigWriter;
        this.buildSteps = buildSteps;
    }


    public void initialize(LanguageSpecBuildInput input) throws MetaborgException {
        final CommonPaths paths = new LangSpecCommonPaths(input.languageSpec().location());
        try {
            paths.srcGenDir().createFolder();
            paths.targetMetaborgDir().createFolder();
        } catch(FileSystemException e) {
            throw new MetaborgException("Initializing directories failed", e);
        }

        for(IBuildStep buildStep : buildSteps) {
            buildStep.execute(LanguageSpecBuildPhase.initialize, input);
        }
    }

    public void generateSources(LanguageSpecBuildInput input, @Nullable IFileAccess access)
        throws IOException, MetaborgException {
        final ISpoofaxLanguageSpec languageSpec = input.languageSpec();
        final FileObject location = languageSpec.location();
        final ISpoofaxLanguageSpecConfig config = languageSpec.config();

        logger.debug("Generating sources for {}", input.languageSpec().location());

        final ContinuousLanguageSpecGenerator generator =
            new ContinuousLanguageSpecGenerator(new GeneratorSettings(location, config), access, config.sdfVersion());
        generator.generateAll();

        componentConfigBuilder.reset();
        componentConfigBuilder.copyFrom(input.languageSpec().config());
        final ILanguageComponentConfig componentConfig = componentConfigBuilder.build(location);
        componentConfigWriter.write(location, componentConfig, access);

        for(IBuildStep buildStep : buildSteps) {
            buildStep.execute(LanguageSpecBuildPhase.generateSources, input);
        }
    }

    public void compile(LanguageSpecBuildInput input) throws MetaborgException {
        logger.debug("Running pre-Java build for {}", input.languageSpec().location());

        initPluto();
        try {
            final String path = input.languageSpec().location().getName().getPath();
            plutoBuild(GenerateSourcesBuilder.request(generateSourcesBuilderInput(input)), path);
        } catch(RequiredBuilderFailed e) {
            if(e.getMessage().contains("no rebuild of failing builder")) {
                throw new MetaborgException(failingRebuildMessage, e);
            } else {
                throw new MetaborgException();
            }
        } catch(RuntimeException e) {
            throw e;
        } catch(Throwable e) {
            throw new MetaborgException(e);
        }

        final CommonPaths paths = new LangSpecCommonPaths(input.languageSpec().location());

        // HACK: compile the main ESV file to make sure that packed.esv file is always available.
        final FileObject mainEsvFile = paths.esvMainFile();
        try {
            if(mainEsvFile.exists()) {
                logger.info("Compiling Main ESV file {}", mainEsvFile);
                // @formatter:off
                final BuildInput buildInput = 
                    new BuildInputBuilder(input.languageSpec())
                    .addSource(mainEsvFile)
                    .addTransformGoal(new CompileGoal())
                    .withMessagePrinter(new StreamMessagePrinter(sourceTextService, false, true, logger))
                    .build(dependencyService, languagePathService);
                // @formatter:on
                final ISpoofaxBuildOutput result = runner.build(buildInput, null, null).schedule().block().result();
                if(!result.success()) {
                    throw new MetaborgException("Compiling Main ESV file failed");
                }
            }
        } catch(FileSystemException e) {
            final String message = logger.format("Could not compile ESV file {}", mainEsvFile);
            throw new MetaborgException(message, e);
        } catch(InterruptedException e) {
            // Ignore
        }

        // HACK: compile the main DS file if available, after generating sources (because ds can depend on Stratego
        // strategies), to generate an interpreter.
        final FileObject mainDsFile = paths.dsMainFile(input.languageSpec().config().strategoName());
        try {
            if(mainDsFile.exists()) {
                logger.info("Compiling Main DynSem file {}", mainDsFile);
                // @formatter:off
                final BuildInput buildInput =
                    new BuildInputBuilder(input.languageSpec())
                    .addSource(mainDsFile)
                    .addTransformGoal(new EndNamedGoal("All to Java"))
                    .withMessagePrinter(new StreamMessagePrinter(sourceTextService, false, true, logger))
                    .build(dependencyService, languagePathService);
                // @formatter:on
                final ISpoofaxBuildOutput result = runner.build(buildInput, null, null).schedule().block().result();
                if(!result.success()) {
                    throw new MetaborgException("Compiling Main DynSem file failed");
                }
            }
        } catch(FileSystemException e) {
            final String message = logger.format("Could not compile DynSem file {}", mainDsFile);
            throw new MetaborgException(message, e);
        } catch(InterruptedException e) {
            // Ignore
        }

        for(IBuildStep buildStep : buildSteps) {
            buildStep.execute(LanguageSpecBuildPhase.compile, input);
        }
    }

    public void pkg(LanguageSpecBuildInput input) throws MetaborgException {
        logger.debug("Packaging language implementation for {}", input.languageSpec().location());

        initPluto();
        try {
            final Origin origin = GenerateSourcesBuilder.origin(generateSourcesBuilderInput(input));
            final String path = input.languageSpec().location().getName().getPath();
            plutoBuild(PackageBuilder.request(packageBuilderInput(input, origin)), path);
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
            buildStep.execute(LanguageSpecBuildPhase.pkg, input);
        }
    }

    public FileObject archive(LanguageSpecBuildInput input) throws MetaborgException {
        logger.debug("Archiving language implementation for {}", input.languageSpec().location());

        initPluto();
        final File archiveFile;
        try {
            final Origin generateSourcesOrigin = GenerateSourcesBuilder.origin(generateSourcesBuilderInput(input));
            final Origin packageOrigin = PackageBuilder.origin(packageBuilderInput(input, generateSourcesOrigin));
            final Origin origin = Origin.Builder().add(generateSourcesOrigin).add(packageOrigin).get();
            final String path = input.languageSpec().location().getName().getPath();
            archiveFile = plutoBuild(ArchiveBuilder.request(archiveBuilderInput(input, origin)), path).val();
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
            buildStep.execute(LanguageSpecBuildPhase.pkg, input);
        }

        return resourceService.resolve(archiveFile);
    }

    public void clean(LanguageSpecBuildInput input) throws MetaborgException {
        final FileObject location = input.languageSpec().location();


        logger.debug("Cleaning {}", location);


        final CommonPaths paths = new LangSpecCommonPaths(location);
        cleanAndLog(paths.srcGenDir());
        cleanAndLog(paths.targetDir());

        try {
            plutoClean(location.getName().getPath());
        } catch(IOException e) {
            throw new MetaborgException("Cleaning Pluto file attributes failed", e);
        }

        for(IBuildStep buildStep : buildSteps) {
            buildStep.execute(LanguageSpecBuildPhase.clean, input);
        }
    }

    private void cleanAndLog(FileObject dir) {
        logger.info("Deleting {}", dir);
        try {
            dir.delete(new AllFileSelector());
        } catch(FileSystemException e) {
            logger.error("Could not delete {}", e, dir);
        }
    }


    private void initPluto() {
        SpoofaxContext.init(injector);
    }

    private <Out extends Output> Out plutoBuild(BuildRequest<?, Out, ?, ?> buildRequest, String path) throws Throwable {
        return BuildManagers.build(buildRequest, new SpoofaxReporting(), path);
    }

    private void plutoClean(String path) throws IOException {
        BuildManagers.resetDynamicAnalysis(path);
    }


    private GenerateSourcesBuilder.Input generateSourcesBuilderInput(LanguageSpecBuildInput input)
        throws FileSystemException, MetaborgException {
        final ISpoofaxLanguageSpec languageSpec = input.languageSpec();
        final ISpoofaxLanguageSpecConfig config = languageSpec.config();
        final FileObject baseLoc = input.languageSpec().location();
        final LangSpecCommonPaths paths = new LangSpecCommonPaths(baseLoc);
        final FileObject buildInfoLoc = paths.plutoBuildInfoDir();
        final SpoofaxContext context = new SpoofaxContext(baseLoc, buildInfoLoc);


        // SDF
        final String sdfModule = config.sdfName();

        final FileObject sdfFileCandidate;
        final SdfVersion sdfVersion = config.sdfVersion();
        final Sdf2tableVersion sdf2tableVersion = config.sdf2tableVersion();
        switch(sdfVersion) {
            case sdf2:
                sdfFileCandidate = paths.syntaxMainFile(sdfModule);
                break;
            case sdf3:
                sdfFileCandidate = paths.syntaxSrcGenMainFile(sdfModule);
                break;
            default:
                throw new MetaborgException("Unknown SDF version: " + sdfVersion);
        }
        final @Nullable File sdfFile;
        if(sdfFileCandidate.exists()) {
            sdfFile = resourceService.localPath(sdfFileCandidate);
        } else {
            sdfFile = null;
        }

        final @Nullable File sdfExternalDef;
        final String sdfExternalDefStr = config.sdfExternalDef();
        if(sdfExternalDefStr != null) {
            final FileObject sdfExternalDefLoc = resourceService.resolve(sdfExternalDefStr);
            if(!sdfExternalDefLoc.exists()) {
                throw new MetaborgException("External SDF definition at " + sdfExternalDefLoc + " does not exist");
            }
            sdfExternalDef = resourceService.localFile(sdfExternalDefLoc);
        } else {
            sdfExternalDef = null;
        }

        final Iterable<FileObject> sdfIncludePaths =
            languagePathService.sourceAndIncludePaths(languageSpec, SpoofaxConstants.LANG_SDF_NAME);
        final FileObject packSdfIncludesReplicateDir = paths.replicateDir().resolveFile("pack-sdf-includes");
        packSdfIncludesReplicateDir.delete(new AllFileSelector());
        final List<File> packSdfIncludePaths = Lists.newArrayList();
        for(FileObject path : sdfIncludePaths) {
            if(!path.exists()) {
                continue;
            }
            packSdfIncludePaths.add(resourceService.localFile(path, packSdfIncludesReplicateDir));
        }

        final Arguments packSdfArgs = config.sdfArgs();

        // SDF completions
        final String sdfCompletionModule = config.sdfName() + "-completion-insertions";
        final @Nullable File sdfCompletionFile;

        FileObject sdfCompletionFileCandidate = null;
        
        if(sdf2tableVersion == Sdf2tableVersion.c) {
            sdfCompletionFileCandidate = paths.syntaxCompletionMainFile(sdfCompletionModule);
        } else if(sdf2tableVersion == Sdf2tableVersion.java) {
            sdfCompletionFileCandidate = paths.syntaxCompletionMainFileNormalized(sdfCompletionModule);
        }
        
        if(sdfCompletionFileCandidate != null && sdfCompletionFileCandidate.exists()) {
            sdfCompletionFile = resourceService.localPath(sdfCompletionFileCandidate);
        } else {
            sdfCompletionFile = null;
        }

        // Meta-SDF
        final String sdfMetaModule = config.metaSdfName();
        final FileObject sdfMetaFileCandidate = paths.syntaxMainFile(sdfMetaModule);
        final @Nullable File sdfMetaFile;
        if(sdfMetaFileCandidate.exists()) {
            sdfMetaFile = resourceService.localPath(sdfMetaFileCandidate);
        } else {
            sdfMetaFile = null;
        }


        // Stratego
        final String strModule = config.strategoName();

        final FileObject strFileCandidate = paths.strMainFile(strModule);
        final @Nullable File strFile;
        if(strFileCandidate.exists()) {
            strFile = resourceService.localPath(strFileCandidate);
        } else {
            strFile = null;
        }
        final String strStratPkg = paths.strJavaTransPkg(config.identifier().id);

        final String strJavaStratPkg = paths.strJavaStratPkg(config.identifier().id);
        final FileObject strJavaStratFileCandidate = paths.strMainJavaStratFile(config.identifier().id);
        final @Nullable File strJavaStratFile;
        if(strJavaStratFileCandidate.exists()) {
            strJavaStratFile = resourceService.localPath(strJavaStratFileCandidate);
        } else {
            strJavaStratFile = null;
        }

        final StrategoFormat strFormat = config.strFormat();

        final @Nullable File strExternalJar;
        final String strExternalJarStr = config.strExternalJar();
        if(strExternalJarStr != null) {
            final FileObject strExternalJarLoc = resourceService.resolve(strExternalJarStr);
            if(!strExternalJarLoc.exists()) {
                throw new MetaborgException("External Stratego JAR at " + strExternalJarLoc + " does not exist");
            }
            strExternalJar = resourceService.localFile(strExternalJarLoc);
        } else {
            strExternalJar = null;
        }
        final String strExternalJarFlags = config.strExternalJarFlags();

        final Iterable<FileObject> strIncludePaths =
            languagePathService.sourceAndIncludePaths(languageSpec, SpoofaxConstants.LANG_STRATEGO_NAME);
        final FileObject strjIncludesReplicateDir = paths.replicateDir().resolveFile("strj-includes");
        strjIncludesReplicateDir.delete(new AllFileSelector());
        final List<File> strjIncludeDirs = Lists.newArrayList();
        for(FileObject path : strIncludePaths) {
            if(!path.exists()) {
                continue;
            }
            strjIncludeDirs.add(resourceService.localFile(path, strjIncludesReplicateDir));
        }

        final Arguments strjArgs = config.strArgs();

        return new GenerateSourcesBuilder.Input(context, config.identifier().id, config.sourceDeps(), sdfModule, sdfFile, sdfVersion,
            sdf2tableVersion, sdfExternalDef, packSdfIncludePaths, packSdfArgs, sdfCompletionModule, sdfCompletionFile,
            sdfMetaModule, sdfMetaFile, strFile, strStratPkg, strJavaStratPkg, strJavaStratFile, strFormat,
            strExternalJar, strExternalJarFlags, strjIncludeDirs, strjArgs);

    }

    private PackageBuilder.Input packageBuilderInput(LanguageSpecBuildInput input, Origin origin)
        throws FileSystemException {
        final ISpoofaxLanguageSpec languageSpec = input.languageSpec();
        final ISpoofaxLanguageSpecConfig config = languageSpec.config();
        final FileObject baseLoc = input.languageSpec().location();
        final LangSpecCommonPaths paths = new LangSpecCommonPaths(baseLoc);
        final FileObject buildInfoLoc = paths.plutoBuildInfoDir();
        final SpoofaxContext context = new SpoofaxContext(baseLoc, buildInfoLoc);

        final StrategoFormat strFormat = config.strFormat();

        final FileObject strJavaStratFileCandidate = paths.strMainJavaStratFile(config.identifier().id);
        final @Nullable File strJavaStratFile;
        if(strJavaStratFileCandidate.exists()) {
            strJavaStratFile = resourceService.localPath(strJavaStratFileCandidate);
        } else {
            strJavaStratFile = null;
        }

        final File javaStratClassesDir =
            resourceService.localPath(paths.strTargetClassesJavaStratDir(config.identifier().id));
        final File dsGeneratedClassesDir = resourceService.localPath(paths.dsTargetClassesGenerateDir());
        final File dsManualClassesDir = resourceService.localPath(paths.dsTargetClassesManualDir());
        final List<File> strJavaStratIncludeDirs =
            Lists.newArrayList(javaStratClassesDir, dsGeneratedClassesDir, dsManualClassesDir);

        return new PackageBuilder.Input(context, config.identifier().id, origin, strFormat, strJavaStratFile,
            strJavaStratIncludeDirs);
    }

    private ArchiveBuilder.Input archiveBuilderInput(LanguageSpecBuildInput input, Origin origin) {
        final ISpoofaxLanguageSpec languageSpec = input.languageSpec();
        final ISpoofaxLanguageSpecConfig config = languageSpec.config();
        final FileObject baseLoc = input.languageSpec().location();
        final LangSpecCommonPaths paths = new LangSpecCommonPaths(baseLoc);
        final FileObject buildInfoLoc = paths.plutoBuildInfoDir();
        final SpoofaxContext context = new SpoofaxContext(baseLoc, buildInfoLoc);

        final Iterable<IExportConfig> exports = config.exports();
        final LanguageIdentifier languageIdentifier = config.identifier();

        return new ArchiveBuilder.Input(context, origin, exports, languageIdentifier);
    }

}
