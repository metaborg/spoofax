package org.metaborg.spoofax.core.style;

public class SortCategory implements ICategory {
    public final String sort;


    public SortCategory(String sort) {
        this.sort = sort;
    }


    @Override public String name() {
        return sort + "._";
    }
}
