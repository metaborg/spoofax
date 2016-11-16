package org.metaborg.meta.core.signature;

import java.io.Serializable;

import org.metaborg.util.iterators.Iterables2;

import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;

public class ConstructorSig implements ISig, Serializable {
    private static final long serialVersionUID = -8751538816115014660L;

    public final String sort;
    public final String constructor;
    public final Iterable<ISortArg> arguments;
    public final int arity;


    public ConstructorSig(String sort, String constructor, Iterable<ISortArg> arguments) {
        this.sort = sort;
        this.constructor = constructor;
        this.arguments = arguments;
        this.arity = Iterables.size(arguments);
    }

    public ConstructorSig(String sort, String constructor, ISortArg... arguments) {
        this(sort, constructor, Iterables2.from(arguments));
    }

    public ConstructorSig(String sort, String constructor) {
        this(sort, constructor, Iterables2.<ISortArg>empty());
    }


    @Override public String sort() {
        return sort;
    }

    @Override public void accept(ISigVisitor visitor) {
        visitor.visitApplication(this);
    }


    @Override public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + sort.hashCode();
        result = prime * result + constructor.hashCode();
        result = prime * result + arity;
        return result;
    }

    @Override public boolean equals(Object obj) {
        if(this == obj)
            return true;
        if(obj == null)
            return false;
        if(getClass() != obj.getClass())
            return false;
        final ConstructorSig other = (ConstructorSig) obj;
        if(!sort.equals(other.sort))
            return false;
        if(!constructor.equals(other.constructor))
            return false;
        if(arity != other.arity)
            return false;
        return true;
    }

    @Override public String toString() {
        return sort + "." + constructor + " = " + Joiner.on(" ").join(arguments);
    }
}
