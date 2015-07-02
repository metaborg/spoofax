package org.metaborg.spoofax.meta.core;

import java.io.IOException;
import java.net.URL;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.AllFileSelector;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.tools.ant.BuildListener;
import org.metaborg.core.resource.IResourceService;
import org.metaborg.spoofax.generator.ProjectGenerator;
import org.metaborg.spoofax.generator.project.ProjectSettings;
import org.metaborg.spoofax.meta.core.ant.IAntRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

public class SpoofaxMetaBuilder {
    private static final Logger log = LoggerFactory.getLogger(SpoofaxMetaBuilder.class);

    private final IResourceService resourceService;
    private final MetaBuildAntRunnerFactory antRunner;


    @Inject public SpoofaxMetaBuilder(IResourceService resourceService, MetaBuildAntRunnerFactory antRunner) {
        this.resourceService = resourceService;
        this.antRunner = antRunner;
    }


    public void initialize(MetaBuildInput input) throws FileSystemException {
        final ProjectSettings settings = input.projectSettings;
        settings.getOutputDirectory().createFolder();
        settings.getLibDirectory().createFolder();
        settings.getGeneratedSourceDirectory().createFolder();
        settings.getGeneratedSyntaxDirectory().createFolder();
    }

    public void generateSources(MetaBuildInput input) throws Exception {
        log.debug("Generating sources for {}", input.project.location());

        final ProjectGenerator generator = new ProjectGenerator(resourceService, input.projectSettings);
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

    public void clean(ProjectSettings projectSettings) throws IOException {
        log.debug("Cleaning {}", projectSettings.location());
        final AllFileSelector selector = new AllFileSelector();
        projectSettings.getJavaTransDirectory().delete(selector);
        projectSettings.getOutputDirectory().delete(selector);
        projectSettings.getGeneratedSourceDirectory().delete(selector);
        projectSettings.getCacheDirectory().delete(selector);
    }
}
