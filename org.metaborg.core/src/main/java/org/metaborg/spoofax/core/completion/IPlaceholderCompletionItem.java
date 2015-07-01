package org.metaborg.spoofax.core.completion;

public interface IPlaceholderCompletionItem extends ICompletionItem {
    public abstract String name();

    public abstract String placeholderText();
}
