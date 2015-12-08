package org.metaborg.core.project;

import org.metaborg.core.project.ILanguageSpec;
import org.metaborg.core.project.IProject;

import javax.annotation.Nullable;

/**
 * Service for getting a language specification.
 */
@Deprecated
public interface ILanguageSpecService {

    /**
     * Gets a language specification from the specified project.
     *
     * @param project The project.
     * @return The language specification; or <code>null</code>
     * when the project is not a language specification project.
     */
    @Nullable
    ILanguageSpec get(IProject project);

}
