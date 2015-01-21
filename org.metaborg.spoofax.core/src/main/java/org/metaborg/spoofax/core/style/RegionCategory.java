package org.metaborg.spoofax.core.style;

import org.metaborg.spoofax.core.messages.ISourceRegion;

public class RegionCategory<T> implements IRegionCategory<T> {
    private final T fragment;
    private final ISourceRegion region;
    private final ICategory categeory;


    public RegionCategory(T fragment, ISourceRegion region, ICategory categeory) {
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

    @Override public ICategory category() {
        return categeory;
    }
}
