package org.metaborg.core.style;

import jakarta.annotation.Nullable;

import org.metaborg.core.source.ISourceRegion;

public class RegionCategory<F> implements IRegionCategory<F> {
    private final ISourceRegion region;
    private final ICategory category;
    private final @Nullable F fragment;


    public RegionCategory(ISourceRegion region, ICategory category, @Nullable F fragment) {
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

    @Override public @Nullable F fragment() {
        return fragment;
    }


    @Override public String toString() {
        return String.format("RegionCategory [region=%s, category=%s, fragment=%s]", region, category, fragment);
    }
}
