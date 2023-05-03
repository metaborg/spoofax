package org.metaborg.spoofax.meta.core.build;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
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
import org.metaborg.core.build.dependency.IDependencyService;
import org.metaborg.core.build.paths.ILanguagePathService;
import org.metaborg.core.config.IExportConfig;
import org.metaborg.core.config.IExportVisitor;
import org.metaborg.core.config.ILanguageComponentConfig;
import org.metaborg.core.config.ILanguageComponentConfigBuilder;
import org.metaborg.core.config.ILanguageComponentConfigWriter;
import org.metaborg.core.config.JSGLRVersion;
import org.metaborg.core.config.LangDirExport;
import org.metaborg.core.config.LangFileExport;
import org.metaborg.core.config.ResourceExport;
import org.metaborg.core.config.Sdf2tableVersion;
import org.metaborg.core.language.ILanguageComponent;
import org.metaborg.core.language.ILanguageIdentifierService;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.language.ILanguageService;
import org.metaborg.core.language.LanguageIdentifier;
import org.metaborg.core.messages.StreamMessagePrinter;
import org.metaborg.core.project.NameUtil;
import org.metaborg.core.resource.IResourceService;
import org.metaborg.core.source.ISourceTextService;
import org.metaborg.spoofax.core.SpoofaxConstants;
import org.metaborg.spoofax.core.build.ISpoofaxBuildOutput;
import org.metaborg.spoofax.core.build.SpoofaxCommonPaths;
import org.metaborg.spoofax.core.dynamicclassloading.DynamicClassLoadingFacet;
import org.metaborg.spoofax.core.processing.ISpoofaxProcessorRunner;
import org.metaborg.spoofax.meta.core.config.ISpoofaxLanguageSpecConfig;
import org.metaborg.spoofax.meta.core.config.LanguageSpecBuildPhase;
import org.metaborg.spoofax.meta.core.config.SdfVersion;
import org.metaborg.spoofax.meta.core.config.StrategoFormat;
import org.metaborg.spoofax.meta.core.config.StrategoVersion;
import org.metaborg.spoofax.meta.core.generator.GeneratorSettings;
import org.metaborg.spoofax.meta.core.generator.general.ContinuousLanguageSpecGenerator;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxContext;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxReporting;
import org.metaborg.spoofax.meta.core.pluto.build.main.ArchiveBuilder;
import org.metaborg.spoofax.meta.core.pluto.build.main.GenerateSourcesBuilder;
import org.metaborg.spoofax.meta.core.pluto.build.main.PackageBuilder;
import org.metaborg.spoofax.meta.core.project.ISpoofaxLanguageSpec;
import org.metaborg.util.cmd.Arguments;
import org.metaborg.util.file.FileUtils;
import org.metaborg.util.file.IFileAccess;
import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.LoggerUtils;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Injector;

import build.pluto.PersistableEntity;
import build.pluto.builder.BuildManager;
import build.pluto.builder.BuildRequest;
import build.pluto.builder.RequiredBuilderFailed;
import build.pluto.dependency.Origin;
import build.pluto.dependency.database.XodusDatabase;
import build.pluto.output.Output;
import jetbrains.exodus.core.execution.JobProcessor;
import jetbrains.exodus.core.execution.ThreadJobProcessorPool;
import mb.pie.api.Supplier;
import mb.pie.api.ValueSupplier;
import mb.resource.fs.FSPath;
import mb.resource.hierarchical.ResourcePath;
import mb.stratego.build.strincr.Stratego2LibInfo;

public class LanguageSpecBuilder implements AutoCloseable {
    private static final ILogger logger = LoggerUtils.logger(LanguageSpecBuilder.class);
    private static final String failingRebuildMessage =
        "Previous build failed and no change in the build input has been observed, not rebuilding. Fix the problem, or clean and rebuild the project to force a rebuild";

    private final Injector injector;
    private final IResourceService resourceService;
    private final ISourceTextService sourceTextService;
    private final ILanguageIdentifierService languageIdentifierService;
    private final IDependencyService dependencyService;
    private final ILanguagePathService languagePathService;
    private final ILanguageService languageService;
    private final ISpoofaxProcessorRunner runner;
    private final Set<IBuildStep> buildSteps;
    private final ILanguageComponentConfigBuilder componentConfigBuilder;
    private final ILanguageComponentConfigWriter componentConfigWriter;


    @Inject public LanguageSpecBuilder(Injector injector, IResourceService resourceService,
        ISourceTextService sourceTextService, ILanguageIdentifierService languageIdentifierService,
        IDependencyService dependencyService, ILanguagePathService languagePathService,
        ILanguageService languageService, ISpoofaxProcessorRunner runner,
        Set<IBuildStep> buildSteps, ILanguageComponentConfigBuilder componentConfigBuilder,
        ILanguageComponentConfigWriter componentConfigWriter) {
        this.injector = injector;
        this.resourceService = resourceService;
        this.sourceTextService = sourceTextService;
        this.languageIdentifierService = languageIdentifierService;
        this.dependencyService = dependencyService;
        this.languagePathService = languagePathService;
        this.languageService = languageService;
        this.runner = runner;
        this.componentConfigBuilder = componentConfigBuilder;
        this.componentConfigWriter = componentConfigWriter;
        this.buildSteps = buildSteps;
    }

    @Override public void close() {
        deinitPluto();
    }


    public void initialize(LanguageSpecBuildInput input) throws MetaborgException {
        final SpoofaxLangSpecCommonPaths paths = new SpoofaxLangSpecCommonPaths(input.languageSpec().location());
        try {
            paths.srcGenDir().createFolder();
            paths.targetMetaborgDir().createFolder();
            paths.str2libsDir().createFolder();
            for(FileObject javaSrcDir : paths.javaSrcDirs()) {
                javaSrcDir.createFolder();
            }
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

        final ContinuousLanguageSpecGenerator generator = new ContinuousLanguageSpecGenerator(
            new GeneratorSettings(location, config), access, config.sdfEnabled(), config.sdfVersion(), config.strVersion());
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
            final String path = path(input);
            plutoBuild(GenerateSourcesBuilder.request(generateSourcesBuilderInput(input)), path);
        } catch(RequiredBuilderFailed e) {
            if(e.getMessage().contains("no rebuild of failing builder")) {
                throw new MetaborgException(failingRebuildMessage, e);
            } else {
                throw new MetaborgException(e);
            }
        } catch(RuntimeException | MetaborgException e) {
            throw e;
        } catch(Throwable e) {
            throw new MetaborgException(e);
        }

        final SpoofaxCommonPaths paths = new SpoofaxLangSpecCommonPaths(input.languageSpec().location());

        // HACK: compile the main ESV file to make sure that packed.esv file is always available.
        final Iterable<FileObject> esvRoots =
            languagePathService.sourcePaths(input.project(), SpoofaxConstants.LANG_ESV_NAME);
        final FileObject mainEsvFile = paths.findEsvMainFile(esvRoots);
        try {
            if(mainEsvFile != null && mainEsvFile.exists()) {
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
        final Iterable<FileObject> dsRoots =
            languagePathService.sourcePaths(input.project(), SpoofaxConstants.LANG_DYNSEM_NAME);
        final FileObject mainDsFile = paths.findDsMainFile(dsRoots, input.languageSpec().config().strategoName());
        try {
            if(mainDsFile != null && mainDsFile.exists()) {
                if(languageIdentifierService.identify(mainDsFile, input.project()) == null) {
                    logger.error("Could not identify DynSem main file {}, please add DynSem as a compile dependency",
                        mainDsFile);
                }

                logger.info("Compiling main DynSem file {}", mainDsFile);
                // @formatter:off
                final BuildInput buildInput =
                    new BuildInputBuilder(input.languageSpec())
                    .addSource(mainDsFile)
                    .addTransformGoal(new EndNamedGoal("Generate interpreter"))
                    .withMessagePrinter(new StreamMessagePrinter(sourceTextService, false, true, logger))
                    .build(dependencyService, languagePathService);
                // @formatter:on
                final ISpoofaxBuildOutput result = runner.build(buildInput, null, null).schedule().block().result();
                if(!result.success()) {
                    logger.error("Compiling main DynSem file {} failed", mainDsFile);
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
            final String path = path(input);
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
            final BuildRequest<?,PackageBuilder.Output,?,?> packageBuildRequest = PackageBuilder.request(packageBuilderInput(input, generateSourcesOrigin));
            final String path = path(input);
            archiveFile = plutoBuild(ArchiveBuilder.request(archiveBuilderInput(input, generateSourcesOrigin, packageBuildRequest)), path).val();
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


        final SpoofaxCommonPaths paths = new SpoofaxLangSpecCommonPaths(location);
        cleanAndLog(paths.srcGenDir());
        cleanAndLog(paths.targetDir());

        try {
            final String path = path(input);
            plutoClean(path);
        } catch(IOException e) {
            throw new MetaborgException("Cleaning Pluto file attributes failed", e);
        }

        GenerateSourcesBuilder.clean();

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

    private void deinitPluto() {
        SpoofaxContext.deinit();
        PersistableEntity.cleanCache(); // Clean Pluto's in-memory cache.
        jetbrains.exodus.log.Log.invalidateSharedCache(); // Clear Xodus' shared cache.
        ThreadJobProcessorPool.getProcessors().forEach(JobProcessor::finish); // Stop Xodus' job processor threads.
    }

    private String path(LanguageSpecBuildInput input) {
        return FileUtils.sanitize(input.languageSpec().location().getName().getFriendlyURI());
    }

    private <Out extends Output> Out plutoBuild(BuildRequest<?, Out, ?, ?> buildRequest, String path) throws Throwable {
        final SpoofaxReporting reporting = new SpoofaxReporting();
        try(final BuildManager buildManager = new BuildManager(reporting, XodusDatabase.createFileDatabase(path))) {
            return buildManager.requireInitially(buildRequest).getBuildResult();
        }
    }

    private void plutoClean(String path) throws IOException {
        final SpoofaxReporting reporting = new SpoofaxReporting();
        try(final BuildManager buildManager = new BuildManager(reporting, XodusDatabase.createFileDatabase(path))) {
            buildManager.resetDynamicAnalysis();
        }
    }


    private GenerateSourcesBuilder.Input generateSourcesBuilderInput(LanguageSpecBuildInput input)
        throws FileSystemException, MetaborgException {
        final ISpoofaxLanguageSpec languageSpec = input.languageSpec();
        final ISpoofaxLanguageSpecConfig config = languageSpec.config();
        final FileObject baseLoc = input.languageSpec().location();
        final SpoofaxLangSpecCommonPaths paths = new SpoofaxLangSpecCommonPaths(baseLoc);
        final FileObject buildInfoLoc = paths.plutoBuildInfoDir();
        final SpoofaxContext context = new SpoofaxContext(baseLoc, buildInfoLoc);

        // Sort source dependencies to ensure deterministic input.
        final ArrayList<LanguageIdentifier> sourceDeps = new ArrayList<>(config.sourceDeps());
        Collections.sort(sourceDeps);

        // SDF
        final Boolean sdfEnabled = config.sdfEnabled();
        final String sdfModule = config.sdfName();
        final JSGLRVersion jsglrVersion = config.jsglrVersion();
        final Boolean checkOverlap = config.checkOverlap();
        final Boolean checkPriorities = config.checkPriorities();

        final FileObject sdfFileCandidate;
        final SdfVersion sdfVersion = config.sdfVersion();
        final Sdf2tableVersion sdf2tableVersion = config.sdf2tableVersion();
        switch(sdfVersion) {
            case sdf2:
                final Iterable<FileObject> sdfRoots =
                    languagePathService.sourcePaths(input.project(), SpoofaxConstants.LANG_SDF_NAME);
                sdfFileCandidate = paths.findSyntaxMainFile(sdfRoots, sdfModule);
                break;
            case sdf3:
                sdfFileCandidate = paths.syntaxSrcGenMainFile(sdfModule);
                break;
            default:
                throw new MetaborgException("Unknown SDF version: " + sdfVersion);
        }
        final @Nullable File sdfFile;
        if(sdfFileCandidate != null && sdfFileCandidate.exists()) {
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
            packSdfIncludePaths.add(resourceService.localFileUpdate(path, packSdfIncludesReplicateDir));
        }

        final Arguments packSdfArgs = config.sdfArgs();

        // SDF completions
        final String sdfCompletionModule = config.sdfName() + "-completion-insertions";
        final @Nullable File sdfCompletionFile;

        FileObject sdfCompletionFileCandidate = null;

        if(!sdf2tableVersion.javaBased) {
            sdfCompletionFileCandidate = paths.syntaxCompletionMainFile(sdfCompletionModule);
        } else {
            sdfCompletionFileCandidate = paths.syntaxCompletionMainFileNormalized(sdfCompletionModule);
        }

        if(sdfCompletionFileCandidate != null && sdfCompletionFileCandidate.exists()) {
            sdfCompletionFile = resourceService.localPath(sdfCompletionFileCandidate);
        } else {
            sdfCompletionFile = null;
        }



        // Meta-SDF
        final Iterable<FileObject> sdfRoots =
            languagePathService.sourcePaths(input.project(), SpoofaxConstants.LANG_SDF_NAME);
        // final String sdfMetaModule = config.metaSdfName();

        List<String> sdfMetaModules = config.sdfMetaFiles();
        final @Nullable List<File> sdfMetaFiles = Lists.newArrayList();

        for(String sdfMetaModule : sdfMetaModules) {
            final FileObject sdfMetaFileCandidate;

            if(config.sdf2tableVersion().javaBased)
                sdfMetaFileCandidate = paths.syntaxSrcGenMainNormFile(sdfMetaModule);
            else
                sdfMetaFileCandidate = paths.findSyntaxMainFile(sdfRoots, sdfMetaModule);

            if(sdfMetaFileCandidate != null && sdfMetaFileCandidate.exists())
                sdfMetaFiles.add(resourceService.localPath(sdfMetaFileCandidate));
        }


        // Stratego
        final Boolean strEnabled = config.strEnabled();
        final String strModule = config.strategoName();

        final LinkedHashSet<Supplier<Stratego2LibInfo>> str2libraries = new LinkedHashSet<>();
        for(LanguageIdentifier sourceDep : sourceDeps) {
            final @Nullable ILanguageImpl sourceDepImpl = languageService.getImpl(sourceDep);
            if(sourceDepImpl == null) {
                continue;
            }
            for(ILanguageComponent sourceDepImplComp : sourceDepImpl.components()) {
                final String[] str2libProject = { null };
                for(IExportConfig export : sourceDepImplComp.config().exports()) {
                    if(str2libProject[0] != null) {
                        break;
                    }
                    export.accept(new IExportVisitor() {
                        @Override public void visit(LangDirExport resource) {}

                        @Override public void visit(LangFileExport resource) {
                            if(resource.language.equals("StrategoLang") && resource.file.endsWith("str2lib")) {
                                str2libProject[0] = resource.file;
                            }
                        }

                        @Override public void visit(ResourceExport resource) {}
                    });
                }
                if(str2libProject[0] != null) {
                    final String str2IncludeDir =
                        "str2-includes/" + sourceDepImplComp.id().toFullFileString() + "-"
                            + sourceDepImplComp.sequenceId();
                    final ResourcePath str2LibFile = new FSPath(resourceService
                        .localFileUpdate(sourceDepImplComp.location().resolveFile(str2libProject[0]),
                            paths.replicateDir().resolveFile(str2IncludeDir)));
                    final @Nullable DynamicClassLoadingFacet facet =
                        sourceDepImplComp.facet(DynamicClassLoadingFacet.class);
                    if(facet == null) {
                        continue;
                    }
                    final ArrayList<ResourcePath> jarFiles =
                        new ArrayList<>(facet.jarFiles.size());
                    for(FileObject file : facet.jarFiles) {
                        jarFiles.add(new FSPath(resourceService.localFileUpdate(file, paths.replicateDir().resolveFile(
                            str2IncludeDir))));
                    }
                    str2libraries.add(
                        new ValueSupplier<>(new Stratego2LibInfo(str2LibFile, jarFiles)));
                }
            }
        }

        final Iterable<FileObject> strRoots =
            languagePathService.sourcePaths(input.project(), SpoofaxConstants.LANG_STRATEGO_NAME);
        final FileObject strFileCandidate = config.strVersion().findStrMainFile(paths, strRoots, strModule);
        final @Nullable File strFile;
        if(strEnabled) {
            if(strFileCandidate != null && strFileCandidate.exists()) {
                strFile = resourceService.localPath(strFileCandidate);
            } else {
                final String fileExtension =
                    config.strVersion() == StrategoVersion.v1 ? "str" : "str2";
                throw new MetaborgException("Cannot find Stratego main file '" + NameUtil.toJavaId(strModule.toLowerCase())
                    + "." + fileExtension + "'.");
            }
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
        if(strEnabled && strExternalJarStr != null) {
            final FileObject strExternalJarLoc = resourceService.resolve(strExternalJarStr);
            if(!strExternalJarLoc.exists()) {
                throw new MetaborgException("External Stratego JAR at " + strExternalJarLoc + " does not exist");
            }
            strExternalJar = resourceService.localFile(strExternalJarLoc);
        } else {
            strExternalJar = null;
        }
        final String strExternalJarFlags = config.strExternalJarFlags();

        final Iterable<FileObject> strIncludePaths = Iterables.concat(
            languagePathService.sourceAndIncludePaths(languageSpec, SpoofaxConstants.LANG_STRATEGO_NAME),
            languagePathService.sourceAndIncludePaths(languageSpec, SpoofaxConstants.LANG_STRATEGO2_NAME));
        final FileObject strjIncludesReplicateDir = paths.replicateDir().resolveFile("strj-includes");
        strjIncludesReplicateDir.delete(new AllFileSelector());
        final List<File> strjIncludeDirs = Lists.newArrayList();
        final List<File> strjIncludeFiles = Lists.newArrayList();
        for(FileObject path : strIncludePaths) {
            if(!path.exists()) {
                continue;
            }
            if(path.isFolder()) {
                strjIncludeDirs.add(resourceService.localFileUpdate(path, strjIncludesReplicateDir));
            }
            if(path.isFile()) {
                strjIncludeFiles.add(resourceService.localFileUpdate(path, strjIncludesReplicateDir));
            }
        }

        final Arguments strjArgs = config.strArgs();

        return new GenerateSourcesBuilder.Input(context, config.identifier(), sourceDeps, sdfEnabled, sdfModule,
            sdfFile, jsglrVersion, sdfVersion, sdf2tableVersion, checkOverlap, checkPriorities, sdfExternalDef,
            packSdfIncludePaths, packSdfArgs, sdfCompletionModule, sdfCompletionFile, sdfMetaModules, sdfMetaFiles,
            strEnabled, strFile, strStratPkg, strJavaStratPkg, strJavaStratFile, strFormat, strExternalJar,
            strExternalJarFlags, strjIncludeDirs, strjIncludeFiles, str2libraries, strjArgs,
            languageSpec.config().strShadowJar(), languageSpec.config().strVersion());

    }

    private PackageBuilder.Input packageBuilderInput(LanguageSpecBuildInput input, Origin origin)
        throws FileSystemException {
        final ISpoofaxLanguageSpec languageSpec = input.languageSpec();
        final ISpoofaxLanguageSpecConfig config = languageSpec.config();
        final FileObject baseLoc = input.languageSpec().location();
        final SpoofaxLangSpecCommonPaths paths = new SpoofaxLangSpecCommonPaths(baseLoc);
        final FileObject buildInfoLoc = paths.plutoBuildInfoDir();
        final SpoofaxContext context = new SpoofaxContext(baseLoc, buildInfoLoc);

        final StrategoFormat strFormat = config.strFormat();

        return new PackageBuilder.Input(context, config.identifier().id, origin, strFormat);
    }

    private ArchiveBuilder.Input archiveBuilderInput(LanguageSpecBuildInput input, Origin origin, BuildRequest<?,PackageBuilder.Output,?,?> packageBuildRequest) {
        final ISpoofaxLanguageSpec languageSpec = input.languageSpec();
        final ISpoofaxLanguageSpecConfig config = languageSpec.config();
        final FileObject baseLoc = input.languageSpec().location();
        final SpoofaxLangSpecCommonPaths paths = new SpoofaxLangSpecCommonPaths(baseLoc);
        final FileObject buildInfoLoc = paths.plutoBuildInfoDir();
        final SpoofaxContext context = new SpoofaxContext(baseLoc, buildInfoLoc);

        final Iterable<IExportConfig> exports = config.exports();
        final LanguageIdentifier languageIdentifier = config.identifier();

        return new ArchiveBuilder.Input(context, origin, packageBuildRequest, exports, languageIdentifier);
    }
}
