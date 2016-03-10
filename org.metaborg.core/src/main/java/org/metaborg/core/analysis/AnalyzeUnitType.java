package org.metaborg.core.analysis;

/**
 * Represents different type of analyze units.
 */
public enum AnalyzeUnitType {
    /**
     * A concrete result.
     */
    Result,
    /**
     * A result that only contains updated messages for a source file that was affected by analysis.
     */
    Affected
}
