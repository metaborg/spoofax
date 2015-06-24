package org.metaborg.spoofax.meta.core;

import java.io.File;
import java.net.URL;

import javax.annotation.Nullable;

import org.apache.tools.ant.BuildListener;
import org.metaborg.spoofax.generator.ProjectGenerator;
import org.metaborg.spoofax.generator.project.ProjectSettings;
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


    public void initialize(MetaBuildInput input) {
        final ProjectSettings settings = input.projectSettings;
        mkdirs(settings.getOutputDirectory());
        mkdirs(settings.getLibDirectory());
        mkdirs(settings.getGeneratedSourceDirectory());
        mkdirs(settings.getGeneratedSyntaxDirectory());
    }

    private void mkdirs(File dir) {
        if(!dir.exists()) {
            dir.mkdirs();
        }
    }

    public void generateSources(MetaBuildInput input) throws Exception {
        log.debug("Generating sources for {}", input.project.location());

        final ProjectGenerator generator = new ProjectGenerator(input.projectSettings);
        generator.generateAll();
    }

    public void compilePreJava(MetaBuildInput input, @Nullable URL[] classpaths, @Nullable BuildListener listener)
        throws Exception {
        log.debug("Running pre-Java build for {}", input.project.location());

        final IAntRunner runner = antRunner.create(input, classpaths, listener);
        runner.execute("generate-sources");
    }

    public void compilePostJava(MetaBuildInput input, @Nullable URL[] classpaths, @Nullable BuildListener listener)
        throws Exception {
        log.debug("Running post-Java build for {}", input.project.location());

        final IAntRunner runner = antRunner.create(input, classpaths, listener);
        runner.execute("package");
    }
}
