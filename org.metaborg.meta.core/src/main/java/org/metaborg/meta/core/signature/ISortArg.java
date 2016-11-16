package org.metaborg.meta.core.signature;

import java.io.Serializable;

public interface ISortArg extends Serializable {
    ISort sort();

    String id();
}
