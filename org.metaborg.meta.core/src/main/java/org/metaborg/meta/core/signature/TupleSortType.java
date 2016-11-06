package org.metaborg.meta.core.signature;

import java.io.Serializable;

public class TupleSortType implements ISortType, Serializable {
    private static final long serialVersionUID = -2087265985985262318L;

    public final Iterable<ISortType> arguments;


    public TupleSortType(Iterable<ISortType> arguments) {
        this.arguments = arguments;
    }
}
