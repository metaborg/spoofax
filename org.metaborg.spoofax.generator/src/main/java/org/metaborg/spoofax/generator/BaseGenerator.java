package org.metaborg.spoofax.generator;

import java.io.File;
import org.metaborg.spoofax.generator.project.ProjectSettings;
import org.metaborg.spoofax.generator.util.MustacheWriter;

public abstract class BaseGenerator {
    
    private final File basedir;
    protected final MustacheWriter writer;

    public BaseGenerator(ProjectSettings projectSettings) {
        this.basedir = projectSettings.getBaseDir();
        this.writer = new MustacheWriter(projectSettings.getBaseDir(),
                new Object[]{ this, projectSettings }, getClass());
    }

}
