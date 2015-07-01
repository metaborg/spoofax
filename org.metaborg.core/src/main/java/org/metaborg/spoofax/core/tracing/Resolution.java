package org.metaborg.spoofax.core.tracing;

import org.metaborg.spoofax.core.source.ISourceLocation;
import org.metaborg.spoofax.core.source.ISourceRegion;

/**
 * Represents a resolution produced by reference resolution.
 */
public class Resolution {
    /**
     * Area in the source file to highlight as a hyperlink.
     */
    public final ISourceRegion highlight;

    /**
     * Resolution targets. Multiple targets indicate resolution to multiple valid locations.
     */
    public final Iterable<ISourceLocation> targets;


    public Resolution(ISourceRegion highlight, Iterable<ISourceLocation> targets) {
        this.highlight = highlight;
        this.targets = targets;
    }
}
