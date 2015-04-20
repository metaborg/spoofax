package org.metaborg.spoofax.core.messages;

public class SourceRegion implements ISourceRegion {
    private static final long serialVersionUID = -3699054067428073315L;
    
	private final int startOffset;
    private final int startRow;
    private final int startColumn;
    private final int endOffset;
    private final int endRow;
    private final int endColumn;


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
        return endOffset - startOffset;
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
        sb.append(" ");
        sb.append(startRow);
        sb.append("-");
        sb.append(endRow);
        sb.append(" ");
        sb.append(startColumn);
        sb.append("-");
        sb.append(endColumn);
        return sb.toString();
    }
}
