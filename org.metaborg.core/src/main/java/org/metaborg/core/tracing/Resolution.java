package org.metaborg.core.tracing;

import javax.annotation.Nullable;

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
    public final Iterable<Resolution.Target> targets;


    public Resolution(ISourceRegion highlight, Iterable<Resolution.Target> targets) {
        this.highlight = highlight;
        this.targets = targets;
    }

    public static class Target {
        public final @Nullable String hyperlinkName;
        public final ISourceLocation location;

        public Target(ISourceLocation location) {
            this(null, location);
        }

        public Target(String hyperlinkName, ISourceLocation location) {
            this.hyperlinkName = hyperlinkName;
            this.location = location;
        }
    }
}
