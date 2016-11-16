package org.metaborg.meta.core.signature;

import java.io.Serializable;

public class PrimitiveSort implements ISort, Serializable {
    private static final long serialVersionUID = -6692741194727404174L;

    public final PrimitiveSortType type;


    public PrimitiveSort(PrimitiveSortType type) {
        this.type = type;
    }


    @Override public void accept(ISortVisitor visitor) {
        visitor.visit(this);
    }


    @Override public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + type.hashCode();
        return result;
    }

    @Override public boolean equals(Object obj) {
        if(this == obj)
            return true;
        if(obj == null)
            return false;
        if(getClass() != obj.getClass())
            return false;
        final PrimitiveSort other = (PrimitiveSort) obj;
        if(type != other.type)
            return false;
        return true;
    }

    @Override public String toString() {
        return type.toString();
    }
}
