package org.metaborg.spoofax.core.completion.jsglr;

import org.metaborg.spoofax.core.completion.IPlaceholderCompletionItem;

public class PlaceholderCompletionItem implements IPlaceholderCompletionItem {
    public final String sort;
    public final String name;


    public PlaceholderCompletionItem(String sort, String name) {
        this.sort = sort;
        this.name = name;
    }


    @Override public String name() {
        return name;
    }

    @Override public String placeholderText() {
        return name;
    }


    @Override public String toString() {
        return "[" + name + "]";
    }
}