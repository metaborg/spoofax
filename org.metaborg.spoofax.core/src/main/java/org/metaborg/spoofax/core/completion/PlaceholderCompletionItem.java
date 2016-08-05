package  org.metaborg.spoofax.core.completion;

import org.metaborg.core.completion.IPlaceholderCompletionItem;

public class PlaceholderCompletionItem implements IPlaceholderCompletionItem {
    /**
     * Serializable because it is necessary to pass an object as a String to Eclipse additional info menu
     */
    private static final long serialVersionUID = -228156717314325153L;
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
        return optional;
    }


    @Override public int startOffset() {
        return startOffset;
    }


    @Override public int endOffset() {
        return endOffset;
    }
}