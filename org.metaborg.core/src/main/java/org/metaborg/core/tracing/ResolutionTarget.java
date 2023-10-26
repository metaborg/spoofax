package org.metaborg.core.tracing;

import jakarta.annotation.Nullable;

import org.metaborg.core.source.ISourceLocation;
import org.metaborg.core.source.ISourceRegion;

public class ResolutionTarget {
    public final String hyperlinkName;
    public final ISourceLocation location;

    public ResolutionTarget(ISourceLocation location) {
        this(null, location);
    }

    public ResolutionTarget(@Nullable String hyperlinkName, ISourceLocation location) {
        this.hyperlinkName = hyperlinkName == null ? defaultHyperlinkName(location) : hyperlinkName;
        this.location = location;
    }

    private static String defaultHyperlinkName(ISourceLocation location) {
        String fileName = location.resource().getName().getBaseName();
        ISourceRegion region = location.region();
        int row = region.startRow();
        int col = region.startColumn();
        return fileName + ":" + (row+1) + ":" + (col+1);
    }
}