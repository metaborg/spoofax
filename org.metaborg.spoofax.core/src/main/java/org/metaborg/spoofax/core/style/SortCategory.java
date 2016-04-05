package org.metaborg.spoofax.core.style;

import org.metaborg.core.style.ICategory;

public class SortCategory implements ICategory {
    private static final long serialVersionUID = 7423414710882093137L;
    
	public final String sort;


    public SortCategory(String sort) {
        this.sort = sort;
    }


    @Override public String name() {
        return sort + "._";
    }


    @Override public String toString() {
        return name();
    }
}
