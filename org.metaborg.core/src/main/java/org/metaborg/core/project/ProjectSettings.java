package org.metaborg.core.project;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.language.LanguageContributionIdentifier;
import org.metaborg.core.language.LanguageIdentifier;
import org.metaborg.util.iterators.Iterables2;

public class ProjectSettings implements IProjectSettings {
    private final LanguageIdentifier identifier;
    private final @Nullable String name;
    private final FileObject location;
    private final Iterable<LanguageIdentifier> compileDependencies;
    private final Iterable<LanguageIdentifier> runtimeDependencies;
    private final Iterable<LanguageContributionIdentifier> languageContributions;


    public ProjectSettings(LanguageIdentifier identifier, @Nullable String name, FileObject location)
        throws ProjectException {
        this(identifier, name, location, Iterables2.<LanguageIdentifier>empty(),
            Iterables2.<LanguageIdentifier>empty(), Iterables2.<LanguageContributionIdentifier>empty());
    }

    public ProjectSettings(LanguageIdentifier identifier, @Nullable String name, FileObject location,
        Iterable<LanguageIdentifier> compileDependencies, Iterable<LanguageIdentifier> runtimeDependencies,
        Iterable<LanguageContributionIdentifier> languageContributions) throws ProjectException {
        if(!NameUtil.isValidLanguageIdentifier(identifier)) {
            throw new ProjectException("Invalid language identifier: " + identifier);
        }
        if(!NameUtil.isValidName(name)) {
            throw new ProjectException("Invalid name: " + name);
        }
        for(LanguageIdentifier compileIdentifier : compileDependencies) {
            if(!NameUtil.isValidLanguageIdentifier(compileIdentifier)) {
                throw new ProjectException("Invalid compile dependency identifier: " + compileIdentifier);
            }
        }
        for(LanguageIdentifier runtimeIdentifier : runtimeDependencies) {
            if(!NameUtil.isValidLanguageIdentifier(runtimeIdentifier)) {
                throw new ProjectException("Invalid runtime dependency identifier: " + runtimeIdentifier);
            }
        }
        for(LanguageContributionIdentifier contributionIdentifier : languageContributions) {
            if(!NameUtil.isValidLanguageIdentifier(contributionIdentifier.identifier)) {
                throw new ProjectException("Invalid language contribution identifier: "
                    + contributionIdentifier.identifier);
            }
            if(!NameUtil.isValidName(contributionIdentifier.name)) {
                throw new ProjectException("Invalid language contribution name: " + name);
            }
        }

        this.identifier = identifier;
        this.name = name;
        this.location = location;
        this.compileDependencies = compileDependencies;
        this.runtimeDependencies = runtimeDependencies;
        this.languageContributions = languageContributions;
    }


    @Override public LanguageIdentifier identifier() {
        return identifier;
    }

    @Override public @Nullable String name() {
        return name;
    }

    @Override public FileObject location() {
        return location;
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
