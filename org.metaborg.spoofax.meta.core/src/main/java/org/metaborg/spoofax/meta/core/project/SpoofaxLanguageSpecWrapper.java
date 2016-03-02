package org.metaborg.spoofax.meta.core.project;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.project.IProject;
import org.metaborg.meta.core.project.ILanguageSpec;
import org.metaborg.spoofax.meta.core.config.ISpoofaxLanguageSpecConfig;
import org.metaborg.spoofax.meta.core.project.ISpoofaxLanguageSpec;
import org.metaborg.spoofax.meta.core.project.ISpoofaxLanguageSpecPaths;

/**
 * Wraps an {@link IProject} to use it as an {@link ILanguageSpec}
 */
public class SpoofaxLanguageSpecWrapper implements IProject, ISpoofaxLanguageSpec {
    private final ISpoofaxLanguageSpecConfig config;
    private final ISpoofaxLanguageSpecPaths paths;
    private final IProject project;


    public SpoofaxLanguageSpecWrapper(ISpoofaxLanguageSpecConfig config, ISpoofaxLanguageSpecPaths paths, IProject project) {
        this.config = config;
        this.paths = paths;
        this.project = project;
    }


    @Override public ISpoofaxLanguageSpecConfig config() {
        return config;
    }

    @Override public ISpoofaxLanguageSpecPaths paths() {
        return paths;
    }

    @Override public FileObject location() {
        return project.location();
    }

    @Override
    public String toString() {
        return this.project.toString();
    }
}
