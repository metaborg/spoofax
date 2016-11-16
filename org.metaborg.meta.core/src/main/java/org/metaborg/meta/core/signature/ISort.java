package org.metaborg.meta.core.signature;

import java.io.Serializable;

public interface ISort extends Serializable {
    void accept(ISortVisitor visitor);
}
