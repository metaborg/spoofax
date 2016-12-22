package org.metaborg.core.completion;

public interface IPlaceholderCompletionItem extends ICompletionItem {
    String name();

    String placeholderText();
    
    int startOffset();
    int endOffset();
    
    boolean optional();
}
