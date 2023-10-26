package org.metaborg.core.transform;

import jakarta.annotation.Nullable;

import org.metaborg.core.source.ISourceRegion;

/**
 * Optional configuration for the transform service.
 */
public interface ITransformConfig {
    /**
     * Selection in the source file the transformation should be applied to, or null if there is no selection.
     */
    public @Nullable ISourceRegion selection();

    /**
     * If this returns true, the transformation should be performed without side effects like writing output files.
     */
    public boolean dryRun();
}
