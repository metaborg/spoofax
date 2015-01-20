package org.metaborg.spoofax.core.style;

import org.metaborg.spoofax.core.messages.ISourceRegion;

public class Category<T> implements ICategory<T> {
    private final T fragment;
    private final ISourceRegion region;
    private final String categeory;


    public Category(T fragment, ISourceRegion region, String categeory) {
        this.fragment = fragment;
        this.region = region;
        this.categeory = categeory;
    }


    @Override public T fragment() {
        return fragment;
    }

    @Override public ISourceRegion region() {
        return region;
    }

    @Override public String category() {
        return categeory;
    }

}
