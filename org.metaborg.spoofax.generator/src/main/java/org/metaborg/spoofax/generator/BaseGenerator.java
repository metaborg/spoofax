package org.metaborg.spoofax.generator;

import org.metaborg.spoofax.generator.project.ProjectSettings;
import org.metaborg.spoofax.generator.util.MustacheWriter;

public abstract class BaseGenerator {
    protected final MustacheWriter writer;


    public BaseGenerator(ProjectSettings projectSettings) {
        this.writer =
            new MustacheWriter(projectSettings.getBaseDir(), new Object[] { this, projectSettings }, getClass());
    }
}
