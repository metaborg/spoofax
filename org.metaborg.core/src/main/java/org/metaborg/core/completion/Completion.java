package org.metaborg.core.completion;


public class Completion implements ICompletion {
    private final Iterable<ICompletionItem> items;


    public Completion(Iterable<ICompletionItem> items) {
        this.items = items;
    }


    @Override public Iterable<ICompletionItem> items() {
        return items;
    }
    
    
}
