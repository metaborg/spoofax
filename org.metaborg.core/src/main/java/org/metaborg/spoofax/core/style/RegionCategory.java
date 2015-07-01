package org.metaborg.spoofax.core.style;

import javax.annotation.Nullable;

import org.metaborg.spoofax.core.source.ISourceRegion;

public class RegionCategory<T> implements IRegionCategory<T> {
    private final ISourceRegion region;
    private final ICategory categeory;
    private final @Nullable T fragment;


    public RegionCategory(ISourceRegion region, ICategory categeory, @Nullable T fragment) {
        this.fragment = fragment;
        this.region = region;
        this.categeory = categeory;
    }


    @Override public ISourceRegion region() {
        return region;
    }

    @Override public ICategory category() {
        return categeory;
    }

    @Override public @Nullable T fragment() {
        return fragment;
    }


    @Override public String toString() {
        return String.format("RegionCategory [region=%s, categeory=%s, fragment=%s]", region, categeory, fragment);
    }
}
