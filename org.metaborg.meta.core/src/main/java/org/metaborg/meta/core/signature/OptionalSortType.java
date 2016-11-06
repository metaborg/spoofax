package org.metaborg.meta.core.signature;

import java.io.Serializable;

public class OptionalSortType implements ISortType, Serializable {
    private static final long serialVersionUID = 6348776962573399591L;

    public final ISortType argument;


    public OptionalSortType(ISortType argument) {
        this.argument = argument;
    }
}
