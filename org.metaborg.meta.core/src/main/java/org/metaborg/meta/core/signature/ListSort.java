package org.metaborg.meta.core.signature;

import java.io.Serializable;

public class ListSort implements ISort, Serializable {
    private static final long serialVersionUID = 7629326194356682162L;

    public final ISort argument;


    public ListSort(ISort argument) {
        this.argument = argument;
    }


    @Override public void accept(ISortVisitor visitor) {
        visitor.visit(this);
    }


    @Override public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + argument.hashCode();
        return result;
    }

    @Override public boolean equals(Object obj) {
        if(this == obj)
            return true;
        if(obj == null)
            return false;
        if(getClass() != obj.getClass())
            return false;
        final ListSort other = (ListSort) obj;
        if(!argument.equals(other.argument))
            return false;
        return true;
    }

    @Override public String toString() {
        return argument.toString() + "*";
    }
}
