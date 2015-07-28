package org.metaborg.spoofax.generator;

import org.metaborg.core.resource.IResourceService;
import org.metaborg.spoofax.generator.project.MustacheProjectSettings;
import org.metaborg.spoofax.generator.project.ProjectSettings;
import org.metaborg.spoofax.generator.util.MustacheWriter;

public abstract class BaseGenerator {
    protected final MustacheWriter writer;


    public BaseGenerator(IResourceService resourceService, ProjectSettings projectSettings) {
        final MustacheProjectSettings mustacheProjectSettings =
            new MustacheProjectSettings(resourceService, projectSettings);
        this.writer =
            new MustacheWriter(mustacheProjectSettings.getBaseDir(), new Object[] { this, mustacheProjectSettings },
                getClass());
    }
}
