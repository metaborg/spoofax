package org.metaborg.core.completion;

public interface IPlaceholderCompletionItem extends ICompletionItem {
    public abstract String name();

    public abstract String placeholderText();
}
