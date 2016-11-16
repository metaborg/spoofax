package org.metaborg.meta.core.signature;

import java.io.Serializable;

public interface ISig extends Serializable {
    String sort();

    void accept(ISigVisitor visitor);
}
