package org.metaborg.core.style;

import javax.annotation.Nullable;

import org.metaborg.core.source.ISourceRegion;

public class RegionStyle<F> implements IRegionStyle<F> {
    private final ISourceRegion region;
    private final IStyle style;
    private final @Nullable F fragment;


    public RegionStyle(ISourceRegion region, IStyle style, @Nullable F fragment) {
        this.fragment = fragment;
        this.region = region;
        this.style = style;
    }



    @Override public ISourceRegion region() {
        return region;
    }

    @Override public IStyle style() {
        return style;
    }

    @Override public @Nullable F fragment() {
        return fragment;
    }


    @Override public String toString() {
        return String.format("RegionStyle [region=%s, style=%s, fragment=%s]", region, style, fragment);
    }
}
