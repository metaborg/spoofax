package org.metaborg.spoofax.core.completion;

import javax.annotation.Nullable;

import org.metaborg.core.language.IFacet;

import com.google.common.collect.Multimap;

public class SyntacticCompletionFacet implements IFacet {
    private final Multimap<Integer, CompletionDefinition> completionDefinitions;


    public SyntacticCompletionFacet(Multimap<Integer, CompletionDefinition> completionDefinitions) {
        this.completionDefinitions = completionDefinitions;
    }


    public @Nullable Iterable<CompletionDefinition> get(int s) {
        return completionDefinitions.get(s);
    }
}
