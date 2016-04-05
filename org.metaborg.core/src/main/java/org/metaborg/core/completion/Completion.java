package org.metaborg.core.completion;


public class Completion implements ICompletion {
    private Iterable<ICompletionItem> items;
    private final String description;
    private final String text;
    
    private final int startOffset;
    private final int endOffset;
    
    private boolean nested;

    public Completion(String description, String text, int startOffset, int endOffset) {
        this.description = description;
        this.text = text;
        this.startOffset = startOffset;
        this.endOffset = endOffset;
        this.nested = false;
    }

    @Override public Iterable<ICompletionItem> items() {
        return items;
    }

    @Override public String toString() {
        return description;
    }
    
    @Override public String text(){
        return text;
    }
    
    @Override
    public boolean isNested(){
        return nested;
    }
    
    @Override
    public void setNested(boolean nested){
        this.nested = nested;
    }

    @Override public int startOffset() {
        return startOffset;
    }

    @Override public int endOffset() {
        return endOffset;
    }
    
    @Override public void setItems(Iterable<ICompletionItem> items){
        this.items = items;
    }
    
    
}
