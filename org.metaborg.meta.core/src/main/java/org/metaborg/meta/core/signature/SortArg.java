package org.metaborg.meta.core.signature;

import java.io.Serializable;

public class SortArg implements ISortArg, Serializable {
    private static final long serialVersionUID = 9067037706536372092L;

    public final ISort sort;
    public final String id;


    public SortArg(ISort sort, String id) {
        this.sort = sort;
        this.id = id;
    }


    @Override public ISort sort() {
        return sort;
    }

    @Override public String id() {
        return id;
    }


    @Override public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + id.hashCode();
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
        final SortArg other = (SortArg) obj;
        if(!id.equals(other.id))
            return false;
        if(!sort.equals(other.sort))
            return false;
        return true;
    }

    @Override public String toString() {
        return id + " : " + sort;
    }
}
