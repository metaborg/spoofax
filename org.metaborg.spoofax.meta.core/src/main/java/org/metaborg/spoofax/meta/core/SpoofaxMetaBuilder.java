package org.metaborg.spoofax.meta.core;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.Set;

import javax.annotation.Nullable;

import build.pluto.buildjava.JarBuilder;

import com.google.common.collect.Lists;

import org.apache.commons.vfs2.AllFileSelector;
import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.tools.ant.BuildListener;
import org.metaborg.core.MetaborgException;
import org.metaborg.core.action.CompileGoal;
import org.metaborg.core.action.EndNamedGoal;
import org.metaborg.core.build.BuildInput;
import org.metaborg.core.build.BuildInputBuilder;
import org.metaborg.core.build.ConsoleBuildMessagePrinter;
import org.metaborg.core.build.dependency.IDependencyService;
import org.metaborg.core.build.paths.ILanguagePathService;
import org.metaborg.core.processing.ICancellationToken;
import org.metaborg.core.project.settings.YAMLProjectSettingsSerializer;
import org.metaborg.core.source.ISourceTextService;
import org.metaborg.spoofax.core.processing.ISpoofaxProcessorRunner;
import org.metaborg.spoofax.core.project.settings.Format;
import org.metaborg.spoofax.core.project.settings.SpoofaxProjectSettings;
import org.metaborg.spoofax.generator.language.ProjectGenerator;
import org.metaborg.spoofax.generator.project.GeneratorProjectSettings;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxContext;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxReporting;
import org.metaborg.spoofax.meta.core.pluto.build.main.GenerateSourcesBuilder;
import org.metaborg.spoofax.meta.core.pluto.build.main.PackageBuilder;
import org.metaborg.spoofax.meta.core.pluto.stamp.DirectoryLastModifiedStamper;
import org.metaborg.util.cmd.Arguments;
import org.metaborg.util.file.FileAccess;
import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.LoggerUtils;
import org.metaborg.util.resource.FileSelectorUtils;

import build.pluto.builder.BuildManagers;
import build.pluto.builder.BuildRequest;
import build.pluto.builder.RequiredBuilderFailed;
import build.pluto.output.Output;
import build.pluto.xattr.Xattr;

import com.google.inject.Inject;
import com.google.inject.Injector;

// TODO Rename to: SpoofaxLanguageSpecBuilder
public class SpoofaxMetaBuilder {
    private static final ILogger logger = LoggerUtils.logger(SpoofaxMetaBuilder.class);
    private static final String failingRebuildMessage =
        "Previous build failed and no change in the build input has been observed, not rebuilding. Fix the problem, or clean and rebuild the project to force a rebuild";

    private final Injector injector;
    private final ISourceTextService sourceTextService;
    private final IDependencyService dependencyService;
    private final ILanguagePathService languagePathService;
    private final ISpoofaxProcessorRunner runner;
    private final MetaBuildAntRunnerFactory antRunner;
    private final Set<IBuildStep> buildSteps;


    @Inject public SpoofaxMetaBuilder(Injector injector, ISourceTextService sourceTextService, IDependencyService dependencyService,
        ILanguagePathService languagePathService, ISpoofaxProcessorRunner runner, MetaBuildAntRunnerFactory antRunner,
        Set<IBuildStep> buildSteps) {
        this.injector = injector;
        this.sourceTextService = sourceTextService;
        this.dependencyService = dependencyService;
        this.languagePathService = languagePathService;
        this.runner = runner;
        this.antRunner = antRunner;
        this.buildSteps = buildSteps;
    }


    public void initialize(MetaBuildInput input) throws FileSystemException {
        final SpoofaxProjectSettings settings = input.settings;
        settings.getIncludeDirectory().createFolder();
        settings.getLibDirectory().createFolder();
        settings.getGenSourceDirectory().createFolder();
        settings.getGenSyntaxDirectory().createFolder();
    }

    public void generateSources(MetaBuildInput input, FileAccess access) throws Exception {
        logger.debug("Generating sources for {}", input.project.location());

        final ProjectGenerator generator = new ProjectGenerator(new GeneratorProjectSettings(input.settings), access);
        generator.generateAll();

        final FileObject settingsFile =
            input.project.location().resolveFile("src-gen").resolveFile("metaborg.generated.yaml");
        settingsFile.createFile();
        YAMLProjectSettingsSerializer.write(settingsFile, input.settings.settings());
        access.addWrite(settingsFile);
    }

    public void compilePreJava(MetaBuildInput input, @Nullable URL[] classpaths, @Nullable BuildListener listener,
        @Nullable ICancellationToken cancellationToken) throws MetaborgException {
        logger.debug("Running pre-Java build for {}", input.project.location());

        for(IBuildStep buildStep : buildSteps) {
            buildStep.compilePreJava(input);
        }

        initPluto();
        try {
            plutoBuild(GenerateSourcesBuilder.request(generateSourcesBuilderInput(input)));
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

        // HACK: compile the main ESV file to make sure that packed.esv file is always available.
        final FileObject mainEsvFile = input.settings.getMainESVFile();
        try {
            if(mainEsvFile.exists()) {
                logger.info("Compiling ESV file {}", mainEsvFile);
                // @formatter:off
                final BuildInput buildInput =
                    new BuildInputBuilder(input.project)
                    .addSource(mainEsvFile)
                    .addTransformGoal(new CompileGoal())
                    .withMessagePrinter(new ConsoleBuildMessagePrinter(sourceTextService, false, true, logger))
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
        final FileObject mainDsFile = input.settings.getMainDsFile();
        try {
            if(mainDsFile.exists()) {
                logger.info("Compiling DynSem file {}", mainDsFile);
                // @formatter:off
                final BuildInput buildInput =
                    new BuildInputBuilder(input.project)
                    .addSource(mainDsFile)
                    .addTransformGoal(new EndNamedGoal("All to Java"))
                    .withMessagePrinter(new ConsoleBuildMessagePrinter(sourceTextService, false, true, logger))
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
    }

    public void compilePostJava(MetaBuildInput input, @Nullable URL[] classpaths, @Nullable BuildListener listener,
        @Nullable ICancellationToken cancellationToken) throws MetaborgException {
        logger.debug("Running post-Java build for {}", input.project.location());

        for(IBuildStep buildStep : buildSteps) {
            buildStep.compilePostJava(input);
        }

        initPluto();
        try {
            plutoBuild(PackageBuilder.request(packageBuilderInput(input)));
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
    }

    public void clean(SpoofaxProjectSettings settings) throws IOException {
        logger.debug("Cleaning {}", settings.location());
        final AllFileSelector selector = new AllFileSelector();
        settings.getStrJavaTransDirectory().delete(selector);
        settings.getIncludeDirectory().delete(selector);
        settings.getGenSourceDirectory().delete(selector);
        settings.getCacheDirectory().delete(selector);
        settings.getDsGeneratedInterpreterJava().delete(FileSelectorUtils.extension("java"));
        Xattr.getDefault().clear();
    }


    private void initPluto() {
        SpoofaxContext.init(injector);
    }

    private <Out extends Output> Out plutoBuild(BuildRequest<?, Out, ?, ?> buildRequest) throws Throwable {
        return BuildManagers.build(buildRequest, new SpoofaxReporting());
    }

    private GenerateSourcesBuilder.Input generateSourcesBuilderInput(MetaBuildInput input) {
        final SpoofaxProjectSettings settings = input.settings;
        final SpoofaxContext context = new SpoofaxContext(
                settings.location(),
                settings.getBuildDirectory());

        final String module = settings.sdfName();
        final String metaModule = settings.metaSdfName();
        final File strategoMainFile = context.toFile(settings.getStrMainFile());
        final File strategoJavaStrategiesMainFile = context.toFile(settings.getStrJavaStrategiesMainFile());

        @Nullable final File externalDef;
        if (settings.externalDef() != null) {
            externalDef = context.toFile(context.resourceService().resolve(settings.externalDef()));
        } else {
            externalDef = null;
        }
        final File packSdfInputPath = context.toFile(settings.getSdfMainFile(module));
        final File packSdfOutputPath = context.toFile(settings.getSdfCompiledDefFile(module));
        final File packMetaSdfInputPath = context.toFile(settings.getSdfMainFile(metaModule));
        final File packMetaSdfOutputPath = context.toFile(settings.getSdfCompiledDefFile(metaModule));
        final File syntaxFolder = context.toFile(settings.getSyntaxDirectory());
        final File genSyntaxFolder = context.toFile(settings.getGenSyntaxDirectory());

        final File makePermissiveOutputPath = context.toFile(settings.getSdfCompiledPermissiveDefFile(module));
        final File sdf2tableOutputPath = context.toFile(settings.getSdfCompiledTableFile(module));

        final File ppGenInputPath = context.toFile(settings.getSdfCompiledDefFile(module));
        final File ppGenOutputPath = context.toFile(settings.getGenPpCompiledFile(module));
        final File afGenOutputPath = context.toFile(settings.getGenPpAfCompiledFile(module));

        final File ppPackInputPath = context.toFile(settings.getPpFile(module));
        final File ppPackOutputPath = context.toFile(settings.getPpAfCompiledFile(module));

        final File sdf2ParenthesizeInputFile = context.toFile(settings.getSdfCompiledDefFile(module));
        final File sdf2ParenthesizeOutputFile = context.toFile(settings.getStrCompiledParenthesizerFile(module));
        final String sdf2ParenthesizeOutputModule = "include/" + module + "-parenthesize";

        final File sdf2RtgInputFile = context.toFile(settings.getSdfCompiledDefFile(module));
        final File sdf2RtgOutputFile = context.toFile(settings.getRtgFile(module));

        final File rtg2SigOutputPath = context.toFile(settings.getStrCompiledSigFile(module));

        @Nullable final File externalJar;
        @Nullable final File target;
        @Nullable final String externalJarFilename = settings.externalJar();
        if (externalJarFilename != null) {
            externalJar = new File(externalJarFilename);
            try {
                target = context.toFile(settings.getIncludeDirectory().resolveFile(externalJar.getName()));
            } catch (FileSystemException e) {
                throw new RuntimeException("Unexpected exception.", e);
            }
        } else {
            externalJar = null;
            target = null;
        }

        final File strjInputFile = context.toFile(settings.getStrMainFile());
        final File strjOutputFile;
        final File strjDepFile;
        if (settings.format() == Format.ctree) {
            strjOutputFile = context.toFile(settings.getStrCompiledCtreeFile());
            strjDepFile = strjOutputFile;
        } else {
            strjOutputFile = context.toFile(settings.getStrJavaMainFile());
            strjDepFile = context.toFile(settings.getStrJavaTransDirectory());
        }
        final File strjCacheDir = context.toFile(settings.getCacheDirectory());

        return new GenerateSourcesBuilder.Input(
                context,
                strategoMainFile,
                strategoJavaStrategiesMainFile,
                module,
                settings.metaSdfName(),
                input.settings.sdfArgs(),
                externalJar,
                target,
                strjInputFile,
                strjOutputFile,
                strjDepFile,
                strjCacheDir,
                settings.strategoArgs(),
                settings.format(),
                settings.strategiesPackageName(),
                settings.externalJarFlags(),
                rtg2SigOutputPath,
                sdf2RtgInputFile,
                sdf2RtgOutputFile,
                sdf2ParenthesizeInputFile,
                sdf2ParenthesizeOutputFile,
                sdf2ParenthesizeOutputModule,
                ppPackInputPath,
                ppPackOutputPath,
                ppGenInputPath,
                ppGenOutputPath,
                afGenOutputPath,
                makePermissiveOutputPath,
                sdf2tableOutputPath,
                externalDef,
                packSdfInputPath,
                packSdfOutputPath,
                packMetaSdfInputPath,
                packMetaSdfOutputPath,
                syntaxFolder,
                genSyntaxFolder);
    }

    private PackageBuilder.Input packageBuilderInput(MetaBuildInput input) throws FileSystemException {
        final SpoofaxProjectSettings settings = input.settings;
        final SpoofaxContext context = new SpoofaxContext(
                settings.location(),
                settings.getBuildDirectory());

        final File strategoMainFile = context.toFile(settings.getStrMainFile());
        final File strategoJavaStrategiesMainFile = context.toFile(settings.getStrJavaStrategiesMainFile());

        final String sdfName = settings.sdfName();

        final File baseDir = context.toFile(settings.getOutputClassesDirectory());

        @Nullable final File externalDef;
        if (settings.externalDef() != null) {
            externalDef = context.toFile(context.resourceService().resolve(settings.externalDef()));
        } else {
            externalDef = null;
        }
        final File packSdfInputPath = context.toFile(settings.getSdfMainFile(settings.sdfName()));
        final File packSdfOutputPath = context.toFile(settings.getSdfCompiledDefFile(settings.sdfName()));
        final File syntaxFolder = context.toFile(settings.getSyntaxDirectory());
        final File genSyntaxFolder = context.toFile(settings.getGenSyntaxDirectory());

        final File makePermissiveOutputPath = context.toFile(settings.getSdfCompiledPermissiveDefFile(settings.sdfName()));
        final File sdf2tableOutputPath = context.toFile(settings.getSdfCompiledTableFile(settings.sdfName()));

        final File ppGenInputPath = context.toFile(settings.getSdfCompiledDefFile(settings.sdfName()));
        final File ppGenOutputPath = context.toFile(settings.getGenPpCompiledFile(settings.sdfName()));
        final File afGenOutputPath = context.toFile(settings.getGenPpAfCompiledFile(settings.sdfName()));

        final File ppPackInputPath = context.toFile(settings.getPpFile(settings.sdfName()));
        final File ppPackOutputPath = context.toFile(settings.getPpAfCompiledFile(settings.sdfName()));

        final File javaJarOutput = context.toFile(settings.getStrCompiledJavaJarFile());
        // TODO: get javajar-includes from project settings?
        // String[] paths = context.props.getOrElse("javajar-includes",
        // context.settings.packageStrategiesPath()).split("[\\s]+");
        final Collection<File> javaJarPaths = Lists.newArrayList(
                context.toFile(settings.getStrCompiledJavaStrategiesDirectory()),
                context.toFile(settings.getDsGeneratedInterpreterCompiledJava()),
                context.toFile(settings.getDsManualInterpreterCompiledJava()));

        final FileObject target = settings.getStrCompiledJavaTransDirectory();
        final File jarTarget = context.toFile(target);
        final File jarOutput = context.toFile(settings.getStrCompiledJarFile());

        final File targetPpAfFile = context.toFile(target.resolveFile(settings.getPpAfName(settings.sdfName())));
        final File targetGenPpAfFile = context.toFile(target.resolveFile(settings.getGenPpAfName(settings.sdfName())));
        final File targetTblFile = context.toFile(target.resolveFile(settings.getSdfTableName(settings.sdfName())));

        return new PackageBuilder.Input(
                context,
                strategoMainFile,
                strategoJavaStrategiesMainFile,
                settings.sdfArgs(),
                baseDir,
                settings.format(),
                javaJarPaths,
                javaJarOutput,
                sdfName,
                jarTarget,
                jarOutput,
                targetPpAfFile,
                targetGenPpAfFile,
                targetTblFile,
                ppPackInputPath,
                ppPackOutputPath,
                ppGenInputPath,
                ppGenOutputPath,
                afGenOutputPath,
                makePermissiveOutputPath,
                sdf2tableOutputPath,
                externalDef,
                packSdfInputPath,
                packSdfOutputPath,
                syntaxFolder,
                genSyntaxFolder);
    }

    private static
    @Nullable
    String relativize(FileObject path, FileObject base) throws FileSystemException {
        final FileName pathName = path.getName();
        final FileName baseName = base.getName();
        if (!baseName.isDescendent(pathName)) {
            return null;
        }
        return baseName.getRelativeName(pathName);
    }
}
