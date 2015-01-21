package org.metaborg.spoofax.core.style;

import org.metaborg.spoofax.core.messages.ISourceRegion;

public interface IRegionCategory<T> {
    public abstract T fragment();

    public abstract ISourceRegion region();

    public abstract ICategory category();
}
