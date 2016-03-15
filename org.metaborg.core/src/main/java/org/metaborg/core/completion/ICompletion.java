package org.metaborg.core.completion;

public interface ICompletion {
    Iterable<ICompletionItem> items();
    String text();
    int startOffset();
    int endOffset();
    void setItems(Iterable<ICompletionItem> items);
    void setNested(boolean nested);
    boolean isNested();
}
