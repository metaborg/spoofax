package org.metaborg.spoofax.meta.core;

import com.google.inject.Inject;
import org.apache.commons.vfs2.AllFileSelector;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.tools.ant.BuildListener;
import org.metaborg.core.build.BuildInput;
import org.metaborg.core.build.BuildInputBuilder;
import org.metaborg.core.build.dependency.IDependencyService;
import org.metaborg.core.build.paths.ILanguagePathService;
import org.metaborg.core.processing.ICancellationToken;
import org.metaborg.core.project.settings.YAMLProjectSettingsSerializer;
import org.metaborg.core.transform.CompileGoal;
import org.metaborg.spoofax.core.processing.ISpoofaxProcessorRunner;
import org.metaborg.spoofax.core.project.settings.ISpoofaxLanguageSpecConfig;
import org.metaborg.spoofax.core.project.settings.SpoofaxProjectSettings;
import org.metaborg.spoofax.generator.language.ProjectGenerator;
import org.metaborg.spoofax.generator.project.GeneratorProjectSettings;
import org.metaborg.spoofax.meta.core.ant.IAntRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.IOException;
import java.net.URL;

public class NewSpoofaxMetaBuilder {
    private static final Logger log = LoggerFactory.getLogger(NewSpoofaxMetaBuilder.class);

    private final IDependencyService dependencyService;
    private final ILanguagePathService languagePathService;
    private final ISpoofaxProcessorRunner runner;

    private final MetaBuildAntRunnerFactory antRunner;


    @Inject public NewSpoofaxMetaBuilder(IDependencyService dependencyService, ILanguagePathService languagePathService,
                                         ISpoofaxProcessorRunner runner, MetaBuildAntRunnerFactory antRunner) {
        this.dependencyService = dependencyService;
        this.languagePathService = languagePathService;
        this.runner = runner;
        this.antRunner = antRunner;
    }


    public void initialize(LanguageSpecBuildInput input) throws FileSystemException {
        final ISpoofaxLanguageSpecConfig config = input.config;
        config.getOutputDirectory().createFolder();
        config.getLibDirectory().createFolder();
        config.getGeneratedSourceDirectory().createFolder();
        config.getGeneratedSyntaxDirectory().createFolder();
    }

    public void generateSources(LanguageSpecBuildInput input) throws Exception {
        log.debug("Generating sources for {}", input.languageSpec.location());

        final ProjectGenerator generator = new ProjectGenerator(new GeneratorProjectSettings(input.languageSpec.location(), input.config));
        generator.generateAll();

        // TODO: Generate config file!
        final FileObject settingsFile =
            input.languageSpec.location().resolveFile("src-gen").resolveFile("metaborg.generated.yaml");
        settingsFile.createFile();
        YAMLProjectSettingsSerializer.write(settingsFile, input.settings.settings());

        // HACK: compile the main ESV file to make sure that packed.esv file is always available.
        final FileObject mainEsvFile = input.config.getMainESVFile();
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

    public void compilePreJava(MetaBuildInput input, @Nullable URL[] classpaths, @Nullable BuildListener listener,
        @Nullable ICancellationToken cancellationToken) throws Exception {
        log.debug("Running pre-Java build for {}", input.project.location());

        final IAntRunner runner = antRunner.create(input, classpaths, listener);
        runner.execute("generate-sources", cancellationToken);
    }

    public void compilePostJava(MetaBuildInput input, @Nullable URL[] classpaths, @Nullable BuildListener listener,
        @Nullable ICancellationToken cancellationToken) throws Exception {
        log.debug("Running post-Java build for {}", input.project.location());

        final IAntRunner runner = antRunner.create(input, classpaths, listener);
        runner.execute("package", cancellationToken);
    }

    public void clean(SpoofaxProjectSettings settings) throws IOException {
        log.debug("Cleaning {}", settings.location());
        final AllFileSelector selector = new AllFileSelector();
        settings.getJavaTransDirectory().delete(selector);
        settings.getOutputDirectory().delete(selector);
        settings.getGeneratedSourceDirectory().delete(selector);
        settings.getCacheDirectory().delete(selector);
    }
}
