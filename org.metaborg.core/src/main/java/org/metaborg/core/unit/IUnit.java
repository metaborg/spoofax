package org.metaborg.core.unit;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;

public interface IUnit {
    boolean detached();

    @Nullable FileObject source();

    <T> IUnitContrib<T> contrib(String id);

    Iterable<IUnitContrib<?>> contribs();
}
