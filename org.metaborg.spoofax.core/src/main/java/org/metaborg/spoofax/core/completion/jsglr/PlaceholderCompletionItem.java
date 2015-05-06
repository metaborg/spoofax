package org.metaborg.spoofax.core.completion.jsglr;

public class PlaceholderCompletionItem implements ICompletionItem {
    public final String sort;
    public final String placeholderName;


    public PlaceholderCompletionItem(String sort, String placeholderName) {
        this.sort = sort;
        this.placeholderName = placeholderName;
    }
}