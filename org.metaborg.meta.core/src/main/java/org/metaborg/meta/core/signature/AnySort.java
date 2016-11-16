package org.metaborg.meta.core.signature;

import java.io.Serializable;

public class AnySort implements ISort, Serializable {
    private static final long serialVersionUID = 3393728398648348985L;


    @Override public void accept(ISortVisitor visitor) {
        visitor.visit(this);
    }


    @Override public String toString() {
        return "Term";
    }
}
