package org.metaborg.spoofax.core.messages;

/**
 * Interface for representing a finite region in source code text. A region has:
 * <ul>
 * <li>Offset - number of characters from the beginning of the source text, with interval [0,#chars).</li>
 * <li>Row - row or line in the source text, with interval [0,#rows).</li>
 * <li>Column - column in the source text, with interval [0,#columns@row).</li>
 * </ul>
 * Both the starting and ending locations for these numbers are available.
 */
public interface ISourceRegion {
    public int startOffset();

    public int startRow();

    public int startColumn();

    public int endOffset();

    public int endRow();

    public int endColumn();
    
    public int length();
}
