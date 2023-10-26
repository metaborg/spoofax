package org.metaborg.spoofax.core.completion;

import jakarta.annotation.Nullable;

import org.metaborg.core.language.IFacet;
import org.metaborg.util.collection.ListMultimap;

public class SyntacticCompletionFacet implements IFacet {
    // Used to be a Multimap from guava, but no idea is it needs to be a list-based one because this class is unused.
    private final ListMultimap<Integer, CompletionDefinition> completionDefinitions;


    public SyntacticCompletionFacet(ListMultimap<Integer, CompletionDefinition> completionDefinitions) {
        this.completionDefinitions = completionDefinitions;
    }


    public @Nullable Iterable<CompletionDefinition> get(int s) {
        return completionDefinitions.get(s);
    }
}
