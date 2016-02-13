package org.metaborg.core.style;

import javax.annotation.Nullable;

import org.metaborg.core.source.ISourceRegion;

public class RegionCategory<T> implements IRegionCategory<T> {
    private final ISourceRegion region;
    private final ICategory category;
    private final @Nullable T fragment;


    public RegionCategory(ISourceRegion region, ICategory category, @Nullable T fragment) {
        this.fragment = fragment;
        this.region = region;
        this.category = category;
    }


    @Override public ISourceRegion region() {
        return region;
    }

    @Override public ICategory category() {
        return category;
    }

    @Override public @Nullable T fragment() {
        return fragment;
    }


    @Override public String toString() {
        return String.format("RegionCategory [region=%s, category=%s, fragment=%s]", region, category, fragment);
    }
}
