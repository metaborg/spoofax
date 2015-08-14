package org.metaborg.core.build.dependency;

import org.metaborg.core.language.IFacet;
import org.metaborg.core.language.LanguageIdentifier;

public class DependencyFacet implements IFacet {
    public final Iterable<LanguageIdentifier> runtimeDependencies;

    public final Iterable<LanguageIdentifier> compileDependencies;


    public DependencyFacet(Iterable<LanguageIdentifier> compileDependencies,
        Iterable<LanguageIdentifier> runtimeDependencies) {
        this.runtimeDependencies = runtimeDependencies;
        this.compileDependencies = compileDependencies;
    }
}
