package org.metaborg.meta.core.signature;

import java.io.Serializable;

public class Sort implements ISort, Serializable {
    private static final long serialVersionUID = -3260968079753164253L;

    public final String sort;


    public Sort(String sort) {
        this.sort = sort;
    }


    @Override public void accept(ISortVisitor visitor) {
        visitor.visit(this);
    }


    @Override public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + sort.hashCode();
        return result;
    }

    @Override public boolean equals(Object obj) {
        if(this == obj)
            return true;
        if(obj == null)
            return false;
        if(getClass() != obj.getClass())
            return false;
        final Sort other = (Sort) obj;
        if(!sort.equals(other.sort))
            return false;
        return true;
    }

    @Override public String toString() {
        return sort;
    }
}
