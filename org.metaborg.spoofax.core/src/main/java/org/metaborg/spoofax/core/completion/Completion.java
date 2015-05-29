package org.metaborg.spoofax.core.completion;


public class Completion implements ICompletion {
    private final Iterable<ICompletionItem> items;
    private final String description;

    public Completion(Iterable<ICompletionItem> items, String description) {
        this.items = items;
        this.description = description;
    }


    @Override public Iterable<ICompletionItem> items() {
        return items;
    }
    
    
    @Override public String toString() {
        return description;
    }
}
