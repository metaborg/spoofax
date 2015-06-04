package org.metaborg.spoofax.core.project;

import org.metaborg.spoofax.core.language.ILanguage;

public interface IDependencyService {
    public abstract Iterable<ILanguage> runtimeDependencies(IProject project);
    public abstract Iterable<ILanguage> compileDependencies(IProject project);
}
