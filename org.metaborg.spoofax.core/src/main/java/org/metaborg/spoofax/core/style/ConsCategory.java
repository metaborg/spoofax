package org.metaborg.spoofax.core.style;

import org.metaborg.core.style.ICategory;

public class ConsCategory implements ICategory {
    private static final long serialVersionUID = -1986900934532701035L;
    
	public final String cons;


    public ConsCategory(String cons) {
        this.cons = cons;
    }


    @Override public String name() {
        return "_." + cons;
    }


    @Override public String toString() {
        return name();
    }
}
