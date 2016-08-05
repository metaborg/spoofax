package org.metaborg.core.completion;

import java.io.Serializable;


public class Completion implements ICompletion, Serializable {
    /**
     * Serializable because it is necessary to pass an object as a String to Eclipse additional info menu
     */
    private static final long serialVersionUID = 6960435974459583999L;
    
    private final String name;
    private final String sort;
    private final String text;
    private final String additionalInfo;

    private String prefix = "";
    private String suffix = "";

    private Iterable<ICompletionItem> items;

    private final int startOffset;
    private final int endOffset;

    private boolean nested;
    private boolean fromOptionalPlaceholder;

    private final CompletionKind kind;

    public Completion(String name, String sort, String text, String additionalInfo, int startOffset, int endOffset,
        CompletionKind kind) {
        this.name = name;
        this.sort = sort;
        this.text = text;
        this.additionalInfo = additionalInfo;
        this.startOffset = startOffset;
        this.endOffset = endOffset;
        this.nested = false;
        this.fromOptionalPlaceholder = false;
        this.kind = kind;
    }

    public Completion(String name, String sort, String text, String additionalInfo, int startOffset, int endOffset,
        CompletionKind kind, String prefix, String suffix) {
        this.name = name;
        this.sort = sort;
        this.text = text;
        this.additionalInfo = additionalInfo;
        this.startOffset = startOffset;
        this.endOffset = endOffset;
        this.nested = false;
        this.fromOptionalPlaceholder = false;
        this.kind = kind;
        this.prefix = prefix;
        this.suffix = suffix;
    }

    @Override public String name() {
        return name;
    }

    @Override public String sort() {
        return sort;
    }

    @Override public String text() {
        return text;
    }

    @Override public String additionalInfo() {
        return additionalInfo;
    }

    @Override public String prefix() {
        return this.prefix;
    }

    @Override public String suffix() {
        return this.suffix;
    }

    @Override public Iterable<ICompletionItem> items() {
        return items;
    }

    @Override public void setItems(Iterable<ICompletionItem> items) {
        this.items = items;
    }

    @Override public int startOffset() {
        return startOffset;
    }

    @Override public int endOffset() {
        return endOffset;
    }

    @Override public boolean isNested() {
        return nested;
    }

    @Override public void setNested(boolean nested) {
        this.nested = nested;
    }

    @Override public boolean fromOptionalPlaceholder() {
        return fromOptionalPlaceholder;
    }

    @Override public void setOptionalPlaceholder(boolean optional) {
        this.fromOptionalPlaceholder = optional;
    }

    @Override public CompletionKind kind() {
        return this.kind;
    }

    @Override public String toString() {
        return name;
    }


}
