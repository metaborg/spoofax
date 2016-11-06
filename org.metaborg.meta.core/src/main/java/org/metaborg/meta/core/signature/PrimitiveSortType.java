package org.metaborg.meta.core.signature;

import java.io.Serializable;

public class PrimitiveSortType implements ISortType, Serializable {
    private static final long serialVersionUID = -6692741194727404174L;

    public final PrimitiveSortKind kind;


    public PrimitiveSortType(PrimitiveSortKind kind) {
        this.kind = kind;
    }
}
