package org.metaborg.core.completion;

import java.io.Serializable;

public interface ICompletion extends Serializable {
    
    /** Serializable because it is necessary to pass an object as a String to Eclipse additional info menu */
    
    String name();
    String sort();
    String text();
    String additionalInfo();
    
    String prefix();
    String suffix();
    
    Iterable<ICompletionItem> items();
    void setItems(Iterable<ICompletionItem> items);
    
    int startOffset();
    int endOffset();
    
    void setNested(boolean nested);
    boolean isNested();
    boolean fromOptionalPlaceholder();
    void setOptionalPlaceholder(boolean optional); 
    int hashCode();
    boolean equals(Object obj);
    
    CompletionKind kind();    
}
