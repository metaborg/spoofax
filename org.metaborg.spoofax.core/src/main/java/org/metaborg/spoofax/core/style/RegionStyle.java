package org.metaborg.spoofax.core.style;

import org.metaborg.spoofax.core.messages.ISourceRegion;

public class RegionStyle<T> implements IRegionStyle<T> {
    private final T fragment;
    private final ISourceRegion region;
    private final IStyle style;


    public RegionStyle(T fragment, ISourceRegion region, IStyle style) {
        this.fragment = fragment;
        this.region = region;
        this.style = style;
    }


    @Override public T fragment() {
        return fragment;
    }

    @Override public ISourceRegion region() {
        return region;
    }

    @Override public IStyle style() {
        return style;
    }
}
