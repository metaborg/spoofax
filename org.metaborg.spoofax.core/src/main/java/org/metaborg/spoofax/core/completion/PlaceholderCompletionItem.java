package  org.metaborg.spoofax.core.completion;

import org.metaborg.core.completion.IPlaceholderCompletionItem;

public class PlaceholderCompletionItem implements IPlaceholderCompletionItem {
    public final String sort;
    public final String name;
    public final int startOffset;
    public final int endOffset;
    
    public final boolean optional;


    public PlaceholderCompletionItem(String sort, int startOffset, int endOffset, boolean optional) {
        this.sort = sort;
        this.name = sort + hashCode();
        this.optional = optional;
        this.startOffset = startOffset;
        this.endOffset = endOffset;
    }


    @Override public String name() {
        return name;
    }

    @Override public String placeholderText() {
        return sort;
    }


    @Override public String toString() {
        return "[[" + name + "]]";
    }


    @Override public boolean optional() {
        // TODO Auto-generated method stub
        return optional;
    }


    @Override public int startOffset() {
        // TODO Auto-generated method stub
        return startOffset;
    }


    @Override public int endOffset() {
        // TODO Auto-generated method stub
        return endOffset;
    }
}