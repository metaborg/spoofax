package org.metaborg.core.project;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.language.LanguageContributionIdentifier;
import org.metaborg.core.language.LanguageIdentifier;

public interface IProjectSettings {
    public abstract LanguageIdentifier identifier();

    public abstract @Nullable String name();

    public abstract FileObject location();

    public abstract Iterable<LanguageIdentifier> compileDependencies();

    public abstract Iterable<LanguageIdentifier> runtimeDependencies();

    public abstract Iterable<LanguageContributionIdentifier> languageContributions();
}
