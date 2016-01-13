package org.metaborg.spoofax.meta.core;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Set;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.AllFileSelector;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.tools.ant.BuildListener;
import org.metaborg.core.MetaborgException;
import org.metaborg.core.action.CompileGoal;
import org.metaborg.core.build.BuildInput;
import org.metaborg.core.build.BuildInputBuilder;
import org.metaborg.core.build.dependency.IDependencyService;
import org.metaborg.core.build.paths.ILanguagePathService;
import org.metaborg.core.processing.ICancellationToken;
import org.metaborg.core.project.settings.YAMLProjectSettingsSerializer;
import org.metaborg.spoofax.core.processing.ISpoofaxProcessorRunner;
import org.metaborg.spoofax.core.project.settings.Format;
import org.metaborg.spoofax.core.project.settings.SpoofaxProjectSettings;
import org.metaborg.spoofax.generator.language.ProjectGenerator;
import org.metaborg.spoofax.generator.project.GeneratorProjectSettings;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxContext;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxReporting;
import org.metaborg.spoofax.meta.core.pluto.build.main.GenerateSourcesBuilder;
import org.metaborg.spoofax.meta.core.pluto.build.main.PackageBuilder;
import org.metaborg.util.cmd.Arguments;
import org.metaborg.util.file.FileAccess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import build.pluto.builder.BuildManagers;
import build.pluto.builder.BuildRequest;
import build.pluto.builder.RequiredBuilderFailed;
import build.pluto.output.Output;
import build.pluto.xattr.Xattr;

import com.google.inject.Inject;
import com.google.inject.Injector;

// TODO Rename to: SpoofaxLanguageSpecBuilder
public class SpoofaxMetaBuilder {
    private static final Logger log = LoggerFactory.getLogger(SpoofaxMetaBuilder.class);
    private static final String failingRebuildMessage =
        "Previous build failed and no change in the build input has been observed, not rebuilding. Fix the problem, or clean and rebuild the project to force a rebuild";

    private final Injector injector;
    private final IDependencyService dependencyService;
    private final ILanguagePathService languagePathService;
    private final ISpoofaxProcessorRunner runner;
    private final MetaBuildAntRunnerFactory antRunner;
    private final Set<IBuildStep> buildSteps;


    @Inject public SpoofaxMetaBuilder(Injector injector, IDependencyService dependencyService,
        ILanguagePathService languagePathService, ISpoofaxProcessorRunner runner, MetaBuildAntRunnerFactory antRunner,
        Set<IBuildStep> buildSteps) {
        this.injector = injector;
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
        log.debug("Generating sources for {}", input.project.location());

        final ProjectGenerator generator = new ProjectGenerator(new GeneratorProjectSettings(input.settings), access);
        generator.generateAll();

        final FileObject settingsFile =
            input.project.location().resolveFile("src-gen").resolveFile("metaborg.generated.yaml");
        settingsFile.createFile();
        YAMLProjectSettingsSerializer.write(settingsFile, input.settings.settings());
        access.addWrite(settingsFile);

        // HACK: compile the main ESV file to make sure that packed.esv file is always available.
        final FileObject mainEsvFile = input.settings.getMainESVFile();
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

    public void compilePreJava(MetaBuildInput input, @Nullable URL[] classpaths, @Nullable BuildListener listener,
        @Nullable ICancellationToken cancellationToken) throws MetaborgException {
        log.debug("Running pre-Java build for {}", input.project.location());

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
    }

    public void compilePostJava(MetaBuildInput input, @Nullable URL[] classpaths, @Nullable BuildListener listener,
        @Nullable ICancellationToken cancellationToken) throws MetaborgException {
        log.debug("Running post-Java build for {}", input.project.location());

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
        log.debug("Cleaning {}", settings.location());
        final AllFileSelector selector = new AllFileSelector();
        settings.getStrJavaTransDirectory().delete(selector);
        settings.getIncludeDirectory().delete(selector);
        settings.getGenSourceDirectory().delete(selector);
        settings.getCacheDirectory().delete(selector);
        Xattr.getDefault().clear();
    }


    private void initPluto() {
        SpoofaxContext.init(injector);
    }

    private <Out extends Output> Out plutoBuild(BuildRequest<?, Out, ?, ?> buildRequest) throws Throwable {
        return BuildManagers.build(buildRequest, new SpoofaxReporting());
    }

    private GenerateSourcesBuilder.Input generateSourcesBuilderInput(MetaBuildInput input) {
        final SpoofaxContext context = new SpoofaxContext(input.settings);
        // input.settings.sdfName() as module
        // input.settings as settings

        final File ppGenInputPath = context.toFile(input.settings.getSdfCompiledDefFile(input.settings.sdfName()));
        final File ppGenOutputPath = context.toFile(input.settings.getGenPpCompiledFile(input.settings.sdfName()));
        final File afGenOutputPath = context.toFile(input.settings.getGenPpAfCompiledFile(input.settings.sdfName()));

        final File ppPackInputPath = context.toFile(input.settings.getPpFile(input.settings.sdfName()));
        final File ppPackOutputPath = context.toFile(input.settings.getPpAfCompiledFile(input.settings.sdfName()));

        final File sdf2ParenthesizeInputFile = context.toFile(input.settings.getSdfCompiledDefFile(input.settings.sdfName()));
        final File sdf2ParenthesizeOutputFile = context.toFile(input.settings.getStrCompiledParenthesizerFile(input.settings.sdfName()));
        final String sdf2ParenthesizeOutputModule = "include/" + input.settings.sdfName() + "-parenthesize";

        final File sdf2RtgInputFile = context.toFile(input.settings.getSdfCompiledDefFile(input.settings.sdfName()));
        final File sdf2RtgOutputFile = context.toFile(input.settings.getRtgFile(input.settings.sdfName()));

        final File rtg2SigOutputPath = context.toFile(input.settings.getStrCompiledSigFile(input.settings.sdfName()));

        @Nullable final File externalJar;
        @Nullable final File target;
        @Nullable final String externalJarFilename = input.settings.externalJar();
        if (externalJarFilename != null) {
            externalJar = new File(externalJarFilename);
            try {
                target = context.toFile(input.settings.getIncludeDirectory().resolveFile(externalJar.getName()));
            } catch (FileSystemException e) {
                throw new RuntimeException("Unexpected exception.", e);
            }
        } else {
            externalJar = null;
            target = null;
        }

        final File strjInputFile = context.toFile(input.settings.getStrMainFile());
        final File strjOutputFile;
        final File strjDepFile;
        if(input.settings.format() == Format.ctree) {
            strjOutputFile = context.toFile(input.settings.getStrCompiledCtreeFile());
            strjDepFile = strjOutputFile;
        } else {
            strjOutputFile = context.toFile(input.settings.getStrJavaMainFile());
            strjDepFile = context.toFile(input.settings.getStrJavaTransDirectory());
        }
        final File strjCacheDir = context.toFile(input.settings.getCacheDirectory());

        return new GenerateSourcesBuilder.Input(
                new SpoofaxContext(input.settings),
                input.settings.sdfName(),
                input.settings.metaSdfName(),
                externalJar,
                target,
                strjInputFile,
                strjOutputFile,
                strjDepFile,
                strjCacheDir,
                input.settings.strategoArgs(),
                input.settings.format(),
                input.settings.strategiesPackageName(),
                input.settings.externalJarFlags(),
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
                afGenOutputPath);
    }

    private PackageBuilder.Input packageBuilderInput(MetaBuildInput input) {
        final SpoofaxContext context = new SpoofaxContext(input.settings);

        final File ppGenInputPath = context.toFile(input.settings.getSdfCompiledDefFile(input.settings.sdfName()));
        final File ppGenOutputPath = context.toFile(input.settings.getGenPpCompiledFile(input.settings.sdfName()));
        final File afGenOutputPath = context.toFile(input.settings.getGenPpAfCompiledFile(input.settings.sdfName()));

        final File ppPackInputPath = context.toFile(input.settings.getPpFile(input.settings.sdfName()));
        final File ppPackOutputPath = context.toFile(input.settings.getPpAfCompiledFile(input.settings.sdfName()));

        return new PackageBuilder.Input(
                new SpoofaxContext(input.settings),
                ppPackInputPath,
                ppPackOutputPath,
                ppGenInputPath,
                ppGenOutputPath,
                afGenOutputPath);
    }
}
