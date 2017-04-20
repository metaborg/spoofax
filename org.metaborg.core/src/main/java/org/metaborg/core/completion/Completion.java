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

    @Override public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((additionalInfo == null) ? 0 : additionalInfo.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((sort == null) ? 0 : sort.hashCode());
        result = prime * result + ((text == null) ? 0 : text.hashCode());
        return result;
    }

    @Override public boolean equals(Object obj) {
        if(this == obj)
            return true;
        if(obj == null)
            return false;
        if(getClass() != obj.getClass())
            return false;
        Completion other = (Completion) obj;
        if(additionalInfo == null) {
            if(other.additionalInfo != null)
                return false;
        } else if(!additionalInfo.equals(other.additionalInfo))
            return false;
        if(name == null) {
            if(other.name != null)
                return false;
        } else if(!name.equals(other.name))
            return false;
        if(sort == null) {
            if(other.sort != null)
                return false;
        } else if(!sort.equals(other.sort))
            return false;
        if(text == null) {
            if(other.text != null)
                return false;
        } else if(!text.equals(other.text))
            return false;
        return true;
    }


}
