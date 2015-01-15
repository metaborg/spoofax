package org.metaborg.spoofax.core.messages;

public interface ISourceRegion {
    public int startOffset();
    
    public int startRow();

    public int startColumn();

    public int endOffset();
    
    public int endRow();

    public int endColumn();
}
