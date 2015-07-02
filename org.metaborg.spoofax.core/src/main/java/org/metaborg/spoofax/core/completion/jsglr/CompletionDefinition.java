package org.metaborg.spoofax.core.completion.jsglr;

import org.metaborg.core.completion.ICompletionItem;
import org.spoofax.interpreter.terms.IStrategoTerm;

public class CompletionDefinition {
    public final IStrategoTerm producedSort;
    public final IStrategoTerm expectedSort;
    public final String description;
    public final Iterable<ICompletionItem> items;


    public CompletionDefinition(IStrategoTerm producedSort, IStrategoTerm expectedSort, String description,
        Iterable<ICompletionItem> items) {
        super();
        this.producedSort = producedSort;
        this.expectedSort = expectedSort;
        this.description = description;
        this.items = items;
    }



}