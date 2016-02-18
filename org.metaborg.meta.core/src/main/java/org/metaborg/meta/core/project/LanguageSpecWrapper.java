package org.metaborg.meta.core.project;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.project.IProject;
import org.metaborg.meta.core.config.ILanguageSpecConfig;

/**
 * Wraps an {@link IProject} to use it as an {@link ILanguageSpec}
 */
public class LanguageSpecWrapper implements IProject, ILanguageSpec {
    private final ILanguageSpecConfig config;
    private final IProject project;


    public LanguageSpecWrapper(ILanguageSpecConfig config, IProject project) {
        this.config = config;
        this.project = project;
    }


    @Override public ILanguageSpecConfig config() {
        return config;
    }

    @Override public FileObject location() {
        return project.location();
    }
}
