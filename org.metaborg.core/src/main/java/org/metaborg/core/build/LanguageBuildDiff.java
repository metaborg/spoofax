package org.metaborg.core.build;

import org.metaborg.core.resource.IdentifiedResourceChange;

public class LanguageBuildDiff {
    public final Iterable<IdentifiedResourceChange> sourceChanges;
    public final Iterable<IdentifiedResourceChange> includeChanges;
    public final LanguageBuildState newState;


    public LanguageBuildDiff(LanguageBuildState newState, Iterable<IdentifiedResourceChange> sourceFileChanges,
        Iterable<IdentifiedResourceChange> includeFileChanges) {
        this.sourceChanges = sourceFileChanges;
        this.includeChanges = includeFileChanges;
        this.newState = newState;
    }
}
