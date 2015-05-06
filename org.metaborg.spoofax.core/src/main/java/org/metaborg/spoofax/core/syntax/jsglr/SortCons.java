package org.metaborg.spoofax.core.syntax.jsglr;

public class SortCons {
    public String sort;
    public String cons;


    public SortCons(String sort, String cons) {
        this.sort = sort;
        this.cons = cons;
    }


    @Override public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + sort.hashCode();
        result = prime * result + cons.hashCode();
        return result;
    }

    @Override public boolean equals(Object obj) {
        if(this == obj)
            return true;
        if(obj == null)
            return false;
        if(getClass() != obj.getClass())
            return false;
        final SortCons other = (SortCons) obj;
        if(!sort.equals(other.sort))
            return false;
        if(!cons.equals(other.cons))
            return false;
        return true;
    }

    @Override public String toString() {
        return sort + "." + cons;
    }
}