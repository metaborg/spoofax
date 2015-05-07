package org.metaborg.spoofax.core.completion.jsglr;

import org.metaborg.spoofax.core.completion.ICompletionItem;

public class CompletionDefinition {
    public final String sort;
    public final String cons;
    public final String description;
    public final Iterable<ICompletionItem> items;


    public CompletionDefinition(String sort, String cons, String description, Iterable<ICompletionItem> items) {
        this.sort = sort;
        this.cons = cons;
        this.description = description;
        this.items = items;
    }
}