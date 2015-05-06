package org.metaborg.spoofax.core.completion.jsglr;


public class StringCompletionItem implements ICompletionItem {
    public final String string;


    public StringCompletionItem(String string) {
        this.string = string;
    }
}