package org.metaborg.spoofax.core.style;

import java.util.Objects;

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

    @Override public boolean equals(Object o) {
        if(this == o)
            return true;
        if(o == null || getClass() != o.getClass())
            return false;
        SortConsCategory that = (SortConsCategory) o;
        return Objects.equals(sort, that.sort) && Objects.equals(cons, that.cons);
    }

    @Override public int hashCode() {
        return Objects.hash(sort, cons);
    }

    @Override public String toString() {
        return name();
    }
}
