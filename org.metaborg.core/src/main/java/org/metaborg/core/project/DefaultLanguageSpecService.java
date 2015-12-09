package org.metaborg.core.project;

import javax.annotation.Nullable;

/**
 * The default implementation of the {@link ILanguageSpecService} interface.
 */
@Deprecated
public final class DefaultLanguageSpecService implements ILanguageSpecService {

    /**
     * {@inheritDoc}
     */
    @Nullable
    @Override
    public ILanguageSpec get(final IProject project) {
        if (project instanceof ILanguageSpec)
            return (ILanguageSpec)project;
        else {
            return null;
        }
    }

}
