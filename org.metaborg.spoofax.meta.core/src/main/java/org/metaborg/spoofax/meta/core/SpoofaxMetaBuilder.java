package org.metaborg.spoofax.meta.core;

import java.io.IOException;
import java.net.URL;

import javax.annotation.Nullable;

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
import org.metaborg.spoofax.core.project.settings.SpoofaxProjectSettings;
import org.metaborg.spoofax.generator.language.ProjectGenerator;
import org.metaborg.spoofax.generator.project.GeneratorProjectSettings;
import org.metaborg.spoofax.meta.core.ant.IAntRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

/**
 * @deprecated Use {@link SpoofaxLanguageSpecBuilder instead}.
 */
@Deprecated
public class SpoofaxMetaBuilder {
    private static final Logger log = LoggerFactory.getLogger(SpoofaxMetaBuilder.class);

    private final IDependencyService dependencyService;
    private final ILanguagePathService languagePathService;
    private final ISpoofaxProcessorRunner runner;

    private final MetaBuildAntRunnerFactory antRunner;


    @Inject public SpoofaxMetaBuilder(IDependencyService dependencyService, ILanguagePathService languagePathService,
        ISpoofaxProcessorRunner runner, MetaBuildAntRunnerFactory antRunner) {
        this.dependencyService = dependencyService;
        this.languagePathService = languagePathService;
        this.runner = runner;
        this.antRunner = antRunner;
    }


    public void initialize(MetaBuildInput input) throws FileSystemException {
        final SpoofaxProjectSettings settings = input.settings;
        settings.getOutputDirectory().createFolder();
        settings.getLibDirectory().createFolder();
        settings.getGeneratedSourceDirectory().createFolder();
        settings.getGeneratedSyntaxDirectory().createFolder();
    }

    public void generateSources(MetaBuildInput input) throws Exception {
        log.debug("Generating sources for {}", input.project.location());

        final ProjectGenerator generator = new ProjectGenerator(new GeneratorProjectSettings(input.settings));
        generator.generateAll();

        final FileObject settingsFile =
            input.project.location().resolveFile("src-gen").resolveFile("metaborg.generated.yaml");
        settingsFile.createFile();
        YAMLProjectSettingsSerializer.write(settingsFile, input.settings.settings());

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
