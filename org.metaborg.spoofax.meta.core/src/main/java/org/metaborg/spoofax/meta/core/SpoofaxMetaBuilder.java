package org.metaborg.spoofax.meta.core;

import java.io.File;

import javax.annotation.Nullable;

import org.metaborg.spoofax.core.build.paths.ILanguagePathService;
import org.metaborg.spoofax.core.resource.IResourceService;
import org.metaborg.spoofax.generator.ProjectGenerator;
import org.metaborg.spoofax.generator.project.ProjectSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

public class SpoofaxMetaBuilder {
    static final Logger log = LoggerFactory.getLogger(SpoofaxMetaBuilder.class);

    private final IResourceService resourceService;
    private final ILanguagePathService languagePathService;


    @Inject public SpoofaxMetaBuilder(IResourceService resourceService, ILanguagePathService languagePathService) {
        this.resourceService = resourceService;
        this.languagePathService = languagePathService;
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
        final ProjectGenerator generator = new ProjectGenerator(input.projectSettings);
        generator.generateAll();
    }

    public void compilePreJava(MetaBuildInput input, @Nullable ClassLoader classLoader) {
        final AntRunner runner = new AntRunner(resourceService, languagePathService, input, classLoader);
        runner.executeTarget("generate-sources");
    }

    public void compilePostJava(MetaBuildInput input, @Nullable ClassLoader classLoader) {
        final AntRunner runner = new AntRunner(resourceService, languagePathService, input, classLoader);
        runner.executeTarget("package");
    }
}
