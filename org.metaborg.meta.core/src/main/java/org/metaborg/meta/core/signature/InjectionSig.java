package org.metaborg.meta.core.signature;

import java.io.Serializable;

public class InjectionSig implements ISig, Serializable {
    private static final long serialVersionUID = 1040121441379063616L;

    public final String sort;
    public final ISort argument;


    public InjectionSig(String sort, ISort argument) {
        this.sort = sort;
        this.argument = argument;
    }


    @Override public String sort() {
        return sort;
    }

    @Override public void accept(ISigVisitor visitor) {
        visitor.visitInjection(this);
    }


    @Override public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + sort.hashCode();
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
        final InjectionSig other = (InjectionSig) obj;
        if(!sort.equals(other.sort))
            return false;
        if(!argument.equals(other.argument))
            return false;
        return true;
    }

    @Override public String toString() {
        return sort + " = " + argument;
    }
}
