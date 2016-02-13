package org.metaborg.core.completion;

import com.google.common.base.Joiner;

public class Completion implements ICompletion {
    private final Iterable<ICompletionItem> items;


    public Completion(Iterable<ICompletionItem> items) {
        this.items = items;
    }


    @Override public Iterable<ICompletionItem> items() {
        return items;
    }
    
    
    @Override public String toString() {
        return Joiner.on("").join(items);
    }
}
