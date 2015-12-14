package org.metaborg.core.project;

import org.apache.commons.vfs2.FileObject;

/**
 * Wraps an {@link IProject} to use it as an {@link ILanguageSpec}
 *
 * @deprecated This class is used only to bridge between the old and new configuration systems.
 */
@Deprecated
public class LanguageSpecProjectWrapper implements IProject, ILanguageSpec {

    private final IProject project;
    public LanguageSpecProjectWrapper(IProject project) {
        this.project = project;
    }

    @Override
    public FileObject location() {
        return this.project.location();
    }
}
