package org.metaborg.spoofax.core.style;

import java.awt.Color;

import org.metaborg.spoofax.core.messages.ISourceRegion;

public interface IStyle<T> {
    public abstract T fragment();

    public abstract ISourceRegion region();

    public abstract Color color();

    public abstract boolean bold();

    public abstract boolean italic();

    public abstract boolean underscore();
}
