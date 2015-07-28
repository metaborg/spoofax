package org.metaborg.spoofax.core.style;

import org.metaborg.core.style.ICategory;

public class SortConsCategory implements ICategory {
    private static final long serialVersionUID = 8423515260404604295L;
    
	public final String sort;
    public final String cons;


    public SortConsCategory(String sort, String cons) {
        this.sort = sort;
        this.cons = cons;
    }


    @Override public String name() {
        return sort + "." + cons;
    }
    
    
    @Override public String toString() {
        return name();
    }
}