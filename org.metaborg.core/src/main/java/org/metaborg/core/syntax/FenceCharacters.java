package org.metaborg.core.syntax;

public class FenceCharacters {
    public final String open;
    public final String close;


    public FenceCharacters(String open, String close) {
        this.open = open;
        this.close = close;
    }


    @Override public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + close.hashCode();
        result = prime * result + open.hashCode();
        return result;
    }

    @Override public boolean equals(Object obj) {
        if(this == obj)
            return true;
        if(obj == null)
            return false;
        if(getClass() != obj.getClass())
            return false;
        final FenceCharacters other = (FenceCharacters) obj;
        if(!close.equals(other.close))
            return false;
        if(!open.equals(other.open))
            return false;
        return true;
    }

    @Override public String toString() {
        return open + " " + close;
    }
}
