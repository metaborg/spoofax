package org.metaborg.core.project.settings;

import java.io.Serializable;

import org.metaborg.core.language.LanguageContributionIdentifier;
import org.metaborg.core.language.LanguageIdentifier;
import org.metaborg.util.iterators.Iterables2;

public class ProjectSettings implements IProjectSettings, Serializable {
    private static final long serialVersionUID = 7235470907344393083L;
    
    private final LanguageIdentifier identifier;
    private final String name;
    private final Iterable<LanguageIdentifier> compileDependencies;
    private final Iterable<LanguageIdentifier> runtimeDependencies;
    private final Iterable<LanguageContributionIdentifier> languageContributions;


    public ProjectSettings(LanguageIdentifier identifier, String name) {
        this(identifier, name, Iterables2.<LanguageIdentifier>empty(), Iterables2.<LanguageIdentifier>empty(),
            Iterables2.<LanguageContributionIdentifier>empty());
    }

    public ProjectSettings(LanguageIdentifier identifier, String name,
        Iterable<LanguageIdentifier> compileDependencies, Iterable<LanguageIdentifier> runtimeDependencies,
        Iterable<LanguageContributionIdentifier> languageContributions) {
        this.identifier = identifier;
        this.name = name;
        this.compileDependencies = compileDependencies;
        this.runtimeDependencies = runtimeDependencies;
        this.languageContributions = languageContributions;
    }


    @Override public LanguageIdentifier identifier() {
        return identifier;
    }

    @Override public String name() {
        return name;
    }

    @Override public Iterable<LanguageIdentifier> compileDependencies() {
        return compileDependencies;
    }

    @Override public Iterable<LanguageIdentifier> runtimeDependencies() {
        return runtimeDependencies;
    }

    @Override public Iterable<LanguageContributionIdentifier> languageContributions() {
        return languageContributions;
    }
}
