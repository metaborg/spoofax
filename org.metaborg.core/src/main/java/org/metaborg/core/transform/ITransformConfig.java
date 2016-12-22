package org.metaborg.core.transform;

/**
 * Optional configuration for the transform service.
 */
public interface ITransformConfig {
    /**
     * If this returns true, the transformation should be performed without side effects like writing output files.
     */
    public boolean dryRun();
}
