package org.metaborg.spoofax.generator;

import org.metaborg.spoofax.generator.project.GeneratorProjectSettings;
import org.metaborg.spoofax.generator.util.MustacheWriter;
import org.metaborg.util.file.FileAccess;

public abstract class BaseGenerator {
    protected final MustacheWriter writer;


    public BaseGenerator(GeneratorProjectSettings settings, FileAccess access) {
        this.writer = new MustacheWriter(settings.location(), new Object[] { this, settings }, getClass(), access);
    }
}
