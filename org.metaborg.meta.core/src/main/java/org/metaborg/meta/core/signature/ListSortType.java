package org.metaborg.meta.core.signature;

import java.io.Serializable;

public class ListSortType implements ISortType, Serializable {
    private static final long serialVersionUID = 7629326194356682162L;

    public final ISortType argument;


    public ListSortType(ISortType argument) {
        this.argument = argument;
    }
}
