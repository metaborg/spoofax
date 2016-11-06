package org.metaborg.meta.core.signature;

import java.io.Serializable;

import javax.annotation.Nullable;

import org.metaborg.util.iterators.Iterables2;

public class Signature implements Serializable {
    private static final long serialVersionUID = -8751538816115014660L;

    public final ISortType sort;
    public final @Nullable String constructor;
    public final Iterable<ISortType> arguments;


    public Signature(ISortType sort, @Nullable String constructor, Iterable<ISortType> arguments) {
        this.sort = sort;
        this.constructor = constructor;
        this.arguments = arguments;
    }

    public Signature(ISortType sort, @Nullable String constructor, ISortType... arguments) {
        this(sort, constructor, Iterables2.from(arguments));
    }

    public Signature(ISortType sort, @Nullable String constructor) {
        this(sort, constructor, Iterables2.<ISortType>empty());
    }

    public Signature(ISortType sort) {
        this(sort, null, Iterables2.<ISortType>empty());
    }
}
