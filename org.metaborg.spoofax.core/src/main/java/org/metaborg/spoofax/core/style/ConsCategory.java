package org.metaborg.spoofax.core.style;

public class ConsCategory implements ICategory {
    public final String cons;


    public ConsCategory(String cons) {
        this.cons = cons;
    }


    @Override public String name() {
        return "_." + cons;
    }
}
