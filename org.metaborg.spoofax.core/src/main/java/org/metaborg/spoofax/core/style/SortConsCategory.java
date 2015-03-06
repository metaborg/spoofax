package org.metaborg.spoofax.core.style;

public class SortConsCategory implements ICategory {
    public final String sort;
    public final String cons;


    public SortConsCategory(String sort, String cons) {
        this.sort = sort;
        this.cons = cons;
    }


    @Override public String name() {
        return sort + "." + cons;
    }
}