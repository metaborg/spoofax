package  org.metaborg.spoofax.core.completion;

import org.metaborg.core.completion.IPlaceholderCompletionItem;

public class PlaceholderCompletionItem implements IPlaceholderCompletionItem {
    public final String sort;
    public final String name;
    public final boolean optional;


    public PlaceholderCompletionItem(String sort, String name, boolean optional) {
        this.sort = sort;
        this.name = name + hashCode();
        this.optional = optional;
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
}