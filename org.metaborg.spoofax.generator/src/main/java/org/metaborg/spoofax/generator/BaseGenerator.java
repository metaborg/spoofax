package org.metaborg.spoofax.generator;

import org.metaborg.spoofax.generator.project.GeneratorProjectSettings;
import org.metaborg.spoofax.generator.util.MustacheWriter;

public abstract class BaseGenerator {
    protected final MustacheWriter writer;


    public BaseGenerator(GeneratorProjectSettings settings) {
        this.writer = new MustacheWriter(settings.location(), new Object[] { this, settings }, getClass());
    }
}
