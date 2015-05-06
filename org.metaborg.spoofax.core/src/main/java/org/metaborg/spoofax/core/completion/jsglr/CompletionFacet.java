package org.metaborg.spoofax.core.completion.jsglr;

import java.util.Map;

import javax.annotation.Nullable;

import org.metaborg.spoofax.core.language.ILanguageFacet;
import org.metaborg.spoofax.core.syntax.jsglr.SortCons;

public class CompletionFacet implements ILanguageFacet {
    private static final long serialVersionUID = -5048070012531904130L;
    
    private final Map<SortCons, CompletionDefinition> completionDefinitions;


    public CompletionFacet(Map<SortCons, CompletionDefinition> completionDefinitions) {
        this.completionDefinitions = completionDefinitions;
    }


    public @Nullable CompletionDefinition get(String sort, String cons) {
        return completionDefinitions.get(new SortCons(sort, cons));
    }
}
