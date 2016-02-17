package org.metaborg.meta.core.project;

import javax.annotation.Nullable;

import org.metaborg.core.project.IProject;

/**
 * @deprecated This class is only used for the configuration system migration.
 */
@Deprecated
public class LegacyLanguageSpecService implements ILanguageSpecService {
    @Nullable @Override public ILanguageSpec get(@Nullable final IProject project) {
        if(project == null)
            return null;

        if(project instanceof ILanguageSpec)
            return (ILanguageSpec) project;
        else
            return new LanguageSpecProjectWrapper(project);
    }
}
