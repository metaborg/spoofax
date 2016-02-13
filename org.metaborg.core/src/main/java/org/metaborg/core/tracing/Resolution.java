package org.metaborg.core.tracing;

import org.metaborg.core.source.ISourceLocation;
import org.metaborg.core.source.ISourceRegion;

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
