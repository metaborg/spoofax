package org.metaborg.spoofax.core.style;

import java.awt.Color;

import javax.annotation.Nullable;

public interface IStyle {
    public abstract @Nullable Color color();
    
    public abstract @Nullable Color backgroundColor();

    public abstract boolean bold();

    public abstract boolean italic();

    public abstract boolean underscore();
}
