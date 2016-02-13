package org.metaborg.core.source;

public class SourceRegion implements ISourceRegion {
    private static final long serialVersionUID = -3699054067428073315L;

    private final int startOffset;
    private final int startRow;
    private final int startColumn;
    private final int endOffset;
    private final int endRow;
    private final int endColumn;


    /**
     * Creates a zero-length source region from a single offset. Row and column fields are set to -1 to indicate that
     * they are not supported.
     * 
     * @param offset
     *            Offset in the source file.
     */
    public SourceRegion(int offset) {
        this.startOffset = offset;
        this.startRow = -1;
        this.startColumn = -1;
        this.endOffset = offset;
        this.endRow = -1;
        this.endColumn = -1;
    }

    /**
     * Creates a source region from a starting and ending offset. Row and column fields are set to -1 to indicate that
     * they are not supported.
     * 
     * @param startOffset
     *            Starting offset in the source file.
     * @param endOffset
     *            Ending offset in the source file.
     */
    public SourceRegion(int startOffset, int endOffset) {
        this.startOffset = startOffset;
        this.startRow = -1;
        this.startColumn = -1;
        this.endOffset = endOffset;
        this.endRow = -1;
        this.endColumn = -1;
    }

    public SourceRegion(int startOffset, int startRow, int startColumn, int endOffset, int endRow, int endColumn) {
        this.startOffset = startOffset;
        this.startRow = startRow;
        this.startColumn = startColumn;
        this.endOffset = endOffset;
        this.endRow = endRow;
        this.endColumn = endColumn;
    }


    @Override public int startOffset() {
        return startOffset;
    }

    @Override public int startRow() {
        return startRow;
    }

    @Override public int startColumn() {
        return startColumn;
    }

    @Override public int endOffset() {
        return endOffset;
    }

    @Override public int endRow() {
        return endRow;
    }

    @Override public int endColumn() {
        return endColumn;
    }

    @Override public int length() {
        return (endOffset - startOffset) + 1;
    }

    @Override public boolean contains(ISourceRegion region) {
        return region.startOffset() >= this.startOffset && region.startOffset() <= this.endOffset()
            && region.endOffset() <= this.endOffset();
    }


    @Override public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + startRow;
        result = prime * result + startColumn;
        result = prime * result + endRow;
        result = prime * result + endColumn;
        return result;
    }

    @Override public boolean equals(Object obj) {
        if(this == obj)
            return true;
        if(obj == null)
            return false;
        if(getClass() != obj.getClass())
            return false;

        final SourceRegion other = (SourceRegion) obj;
        if(startRow != other.startRow)
            return false;
        if(startColumn != other.startColumn)
            return false;
        if(endRow != other.endRow)
            return false;
        if(endColumn != other.endColumn)
            return false;

        return true;
    }

    @Override public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(startOffset);
        sb.append("-");
        sb.append(endOffset);
        if(startRow >= 0 && endRow >= 0) {
            sb.append(" ");
            sb.append(startRow);
            sb.append("-");
            sb.append(endRow);
        }
        if(startColumn >= 0 && endColumn >= 0) {
            sb.append(" ");
            sb.append(startColumn);
            sb.append("-");
            sb.append(endColumn);
        }
        return sb.toString();
    }
}
