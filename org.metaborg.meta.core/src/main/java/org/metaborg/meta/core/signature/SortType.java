package org.metaborg.meta.core.signature;

import java.io.Serializable;

public class SortType implements ISortType, Serializable {
    private static final long serialVersionUID = -3260968079753164253L;

    public final String sort;


    public SortType(String sort) {
        this.sort = sort;
    }
}
