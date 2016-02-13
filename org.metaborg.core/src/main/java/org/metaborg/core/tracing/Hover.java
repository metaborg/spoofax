package org.metaborg.core.tracing;

import org.metaborg.core.source.ISourceRegion;

/**
 * Represents hover information produced by the hover service.
 */
public class Hover {
    /**
     * Region in the source file where the hover information is for.
     */
    public final ISourceRegion region;

    /**
     * Text to show as hover information.
     */
    public final String text;


    public Hover(ISourceRegion region, String text) {
        this.region = region;
        this.text = text;
    }
}
