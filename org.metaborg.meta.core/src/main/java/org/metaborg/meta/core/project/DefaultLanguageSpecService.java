package org.metaborg.meta.core.project;

import javax.annotation.Nullable;

import org.metaborg.core.project.IProject;

/**
 * The default implementation of the {@link ILanguageSpecService} interface.
 */
public final class DefaultLanguageSpecService implements ILanguageSpecService {
    @Nullable @Override public ILanguageSpec get(@Nullable final IProject project) {
        if(project == null)
            return null;

        if(project instanceof ILanguageSpec)
            return (ILanguageSpec) project;
        else {
            return null;
        }
    }
}
