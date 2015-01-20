package org.metaborg.spoofax.core.style;

import java.awt.Color;

import org.metaborg.spoofax.core.messages.ISourceRegion;

public class Style<T> implements IStyle<T> {
    private final T fragment;
    private final ISourceRegion region;
    private final Color color;
    private final boolean bold;
    private final boolean italic;
    private final boolean underscore;


    public Style(T fragment, ISourceRegion region, Color color, boolean bold, boolean italic,
        boolean underscore) {
        super();
        this.fragment = fragment;
        this.region = region;
        this.color = color;
        this.bold = bold;
        this.italic = italic;
        this.underscore = underscore;
    }


    @Override public T fragment() {
        return fragment;
    }

    @Override public ISourceRegion region() {
        return region;
    }

    @Override public Color color() {
        return color;
    }

    @Override public boolean bold() {
        return bold;
    }

    @Override public boolean italic() {
        return italic;
    }

    @Override public boolean underscore() {
        return underscore;
    }
}
