package org.metaborg.core.source;

import java.io.Serializable;

/**
 * Interface for representing a finite region in source code text. A region has:
 * <ul>
 * <li>Offset - number of characters from the beginning of the source text, with interval [0,#chars).</li>
 * <li>Row - row or line in the source text, with interval [0,#rows), or -1 if the row is not supported.</li>
 * <li>Column - column in the source text, with interval [0,#columns@row), or -1 if the column is not supported.</li>
 * </ul>
 * Both the starting and ending numbers are inclusive.
 */
public interface ISourceRegion extends Serializable {
    /**
     * @return Inclusive starting offset, the number of characters from the beginning of the source text with interval
     *         [0,#chars).
     */
    public int startOffset();

    /**
     * @return Inclusive starting row or line in the source text with interval [0,#rows), or -1 if not supported by this
     *         source region.
     */
    public int startRow();

    /**
     * @return Inclusive starting column in the source text with interval [0,#columns@row), or -1 if not supported by
     *         this source region.
     */
    public int startColumn();

    /**
     * @return Inclusive ending offset, the number of characters from the beginning of the source text with interval
     *         [0,#chars).
     */
    public int endOffset();

    /**
     * @return Inclusive ending row or line in the source text with interval [0,#rows), or -1 if not supported by this
     *         source region.
     */
    public int endRow();

    /**
     * @return Inclusive ending column in the source text with interval [0,#columns@row), or -1 if not supported by this
     *         source region.
     */
    public int endColumn();

    /**
     * @return Length of the source region.
     */
    public int length();

    /**
     * Checks if this region contains given region.
     * 
     * @param region
     *            Other region to check.
     * @return True if this region contains given region, false otherwise.
     */
    public boolean contains(ISourceRegion region);
}
