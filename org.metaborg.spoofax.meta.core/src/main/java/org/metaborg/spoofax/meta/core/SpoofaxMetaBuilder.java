package org.metaborg.spoofax.meta.core;

import java.io.IOException;
import java.net.URL;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.AllFileSelector;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.tools.ant.BuildListener;
import org.metaborg.core.processing.ICancellationToken;
import org.metaborg.core.project.settings.YAMLProjectSettingsSerializer;
import org.metaborg.spoofax.core.project.settings.SpoofaxProjectSettings;
import org.metaborg.spoofax.generator.ProjectGenerator;
import org.metaborg.spoofax.generator.project.GeneratorProjectSettings;
import org.metaborg.spoofax.meta.core.ant.IAntRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

public class SpoofaxMetaBuilder {
    private static final Logger log = LoggerFactory.getLogger(SpoofaxMetaBuilder.class);

    private final MetaBuildAntRunnerFactory antRunner;


    @Inject public SpoofaxMetaBuilder(MetaBuildAntRunnerFactory antRunner) {
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
