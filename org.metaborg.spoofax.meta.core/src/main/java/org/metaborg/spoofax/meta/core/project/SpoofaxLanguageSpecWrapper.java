package org.metaborg.spoofax.meta.core.project;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.project.IProject;
import org.metaborg.meta.core.project.ILanguageSpec;
import org.metaborg.spoofax.meta.core.config.ISpoofaxLanguageSpecConfig;

/**
 * Wraps an {@link IProject} to use it as an {@link ILanguageSpec}
 */
public class SpoofaxLanguageSpecWrapper implements IProject, ISpoofaxLanguageSpec {
    private final ISpoofaxLanguageSpecConfig config;
    private final IProject project;


    public SpoofaxLanguageSpecWrapper(ISpoofaxLanguageSpecConfig config, IProject project) {
        this.config = config;
        this.project = project;
    }


    @Override public ISpoofaxLanguageSpecConfig config() {
        return config;
    }

    @Override public FileObject location() {
        return project.location();
    }

    @Override public String toString() {
        return project.toString();
    }
}
