package org.metaborg.core.syntax;

public class MultiLineCommentCharacters {
    public final String prefix;
    public final String postfix;


    public MultiLineCommentCharacters(String prefix, String postfix) {
        this.prefix = prefix;
        this.postfix = postfix;
    }


    @Override public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + postfix.hashCode();
        result = prime * result + prefix.hashCode();
        return result;
    }

    @Override public boolean equals(Object obj) {
        if(this == obj)
            return true;
        if(obj == null)
            return false;
        if(getClass() != obj.getClass())
            return false;
        final MultiLineCommentCharacters other = (MultiLineCommentCharacters) obj;
        if(!postfix.equals(other.postfix))
            return false;
        if(!prefix.equals(other.prefix))
            return false;
        return true;
    }

    @Override public String toString() {
        return prefix + " " + postfix;
    }
}
