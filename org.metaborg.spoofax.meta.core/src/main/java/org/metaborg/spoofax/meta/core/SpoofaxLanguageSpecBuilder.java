package org.metaborg.spoofax.meta.core;

import com.google.inject.Inject;
import org.apache.commons.vfs2.AllFileSelector;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.tools.ant.BuildListener;
import org.metaborg.core.build.BuildInput;
import org.metaborg.core.build.BuildInputBuilder;
import org.metaborg.core.build.dependency.IDependencyService;
import org.metaborg.core.build.dependency.INewDependencyService;
import org.metaborg.core.build.paths.ILanguagePathService;
import org.metaborg.core.processing.ICancellationToken;
import org.metaborg.core.project.settings.ILanguageSpecConfigService;
import org.metaborg.core.project.settings.YAMLProjectSettingsSerializer;
import org.metaborg.core.project.settings.YamlConfigurationReaderWriter;
import org.metaborg.core.transform.CompileGoal;
import org.metaborg.spoofax.core.processing.ISpoofaxProcessorRunner;
import org.metaborg.spoofax.core.project.ILanguageSpecStructureService;
import org.metaborg.spoofax.core.project.ISpoofaxLanguageSpecPaths;
import org.metaborg.spoofax.core.project.settings.ISpoofaxLanguageSpecConfig;
import org.metaborg.spoofax.core.project.settings.SpoofaxProjectSettings;
import org.metaborg.spoofax.generator.language.NewProjectGenerator;
import org.metaborg.spoofax.generator.project.LanguageSpecGeneratorScope;
import org.metaborg.spoofax.meta.core.ant.IAntRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.IOException;
import java.net.URL;

public class SpoofaxLanguageSpecBuilder {
    private static final Logger log = LoggerFactory.getLogger(SpoofaxLanguageSpecBuilder.class);

    private final INewDependencyService dependencyService;
    private final ILanguagePathService languagePathService;
    private final ISpoofaxProcessorRunner runner;
    private final ILanguageSpecStructureService languageSpecStructureService;
    private final ILanguageSpecConfigService<ISpoofaxLanguageSpecConfig> languageSpecConfigService;

    private final NewMetaBuildAntRunnerFactory antRunner;


    @Inject public SpoofaxLanguageSpecBuilder(INewDependencyService dependencyService, ILanguagePathService languagePathService,
                                              ILanguageSpecStructureService languageSpecStructureService,
                                              ISpoofaxProcessorRunner runner, NewMetaBuildAntRunnerFactory antRunner,
                                              ILanguageSpecConfigService<ISpoofaxLanguageSpecConfig> languageSpecConfigService) {
        this.dependencyService = dependencyService;
        this.languagePathService = languagePathService;
        this.languageSpecStructureService = languageSpecStructureService;
        this.runner = runner;
        this.antRunner = antRunner;
        this.languageSpecConfigService = languageSpecConfigService;
    }


    public void initialize(LanguageSpecBuildInput input) throws FileSystemException {
        ISpoofaxLanguageSpecPaths paths;
        paths.outputDirectory().createFolder();
        paths.libDirectory().createFolder();
        paths.generatedSourceDirectory().createFolder();
        paths.generatedSyntaxDirectory().createFolder();
    }

    public void generateSources(LanguageSpecBuildInput input, ISpoofaxLanguageSpecPaths paths) throws Exception {
        log.debug("Generating sources for {}", input.languageSpec.location());

        final NewProjectGenerator generator = new NewProjectGenerator(new LanguageSpecGeneratorScope(input.languageSpec.location(), input.config));
        generator.generateAll();

        this.languageSpecConfigService.set(input.languageSpec, input.config);

        // HACK: compile the main ESV file to make sure that packed.esv file is always available.
        final FileObject mainEsvFile = this.languageSpecStructureService.getMainESVFile(input.languageSpec.location(), input.config);
        if(mainEsvFile.exists()) {
            // @formatter:off
            final BuildInput buildInput =
                new BuildInputBuilder(input.languageSpec)
                .addSource(mainEsvFile)
                .addTransformGoal(new CompileGoal())
                .build(dependencyService, languagePathService);
            // @formatter:on
            runner.build(buildInput, null, null).schedule().block();
        }
    }

    public void compilePreJava(LanguageSpecBuildInput input, @Nullable URL[] classpaths, @Nullable BuildListener listener,
        @Nullable ICancellationToken cancellationToken) throws Exception {
        log.debug("Running pre-Java build for {}", input.languageSpec.location());

        final IAntRunner runner = antRunner.create(input, classpaths, listener);
        runner.execute("generate-sources", cancellationToken);
    }

    public void compilePostJava(LanguageSpecBuildInput input, @Nullable URL[] classpaths, @Nullable BuildListener listener,
        @Nullable ICancellationToken cancellationToken) throws Exception {
        log.debug("Running post-Java build for {}", input.languageSpec.location());

        final IAntRunner runner = antRunner.create(input, classpaths, listener);
        runner.execute("package", cancellationToken);
    }

    public void clean(ISpoofaxLanguageSpecPaths paths) throws IOException {
        log.debug("Cleaning {}", paths.root());

        final AllFileSelector selector = new AllFileSelector();
        paths.javaTransDirectory().delete(selector);
        paths.output().delete(selector);
        paths.generatedSourceDirectory().delete(selector);
        paths.cacheDirectory().delete(selector);
    }
}
