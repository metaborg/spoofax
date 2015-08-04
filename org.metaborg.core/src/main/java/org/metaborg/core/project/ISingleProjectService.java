package org.metaborg.core.project;

import javax.annotation.Nullable;

import org.metaborg.core.MetaborgRuntimeException;

/**
 * Specialization of the {@link IProjectService} that supports a single project that can be set once.
 */
public interface ISingleProjectService extends IProjectService {
    /**
     * @return Single project or null if it has not been set yet.
     */
    public abstract @Nullable IProject get();

    /**
     * @param project
     *            Sets the single project.
     * @throws MetaborgRuntimeException
     *             When the project has already been set before.
     */
    public abstract void set(IProject project);
}
