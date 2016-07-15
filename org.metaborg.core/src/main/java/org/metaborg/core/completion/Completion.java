package org.metaborg.core.completion;


public class Completion implements ICompletion {
    private Iterable<ICompletionItem> items;
    private final String description;
    private final String text;
    private final CompletionKind kind;
    private final int startOffset;
    private final int endOffset;
    private String prefix = null;
    private String suffix = null;
    
    private boolean nested;

    public Completion(String description, String text, int startOffset, int endOffset, CompletionKind kind) {
        this.description = description;
        this.text = text;
        this.startOffset = startOffset;
        this.endOffset = endOffset;
        this.nested = false;
        this.kind = kind;
    }
    
    public Completion(String description, String text, int startOffset, int endOffset, CompletionKind kind, String prefix, String suffix) {
        this.description = description;
        this.text = text;
        this.startOffset = startOffset;
        this.endOffset = endOffset;
        this.nested = false;
        this.kind = kind;
        this.prefix = prefix;
        this.suffix = suffix;
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

    @Override public CompletionKind kind() {
        return this.kind;
    }

    @Override public String prefix() {
        // TODO Auto-generated method stub
        return this.prefix;
    }

    @Override public String suffix() {
        // TODO Auto-generated method stub
        return this.suffix;
    }
    
    
}
