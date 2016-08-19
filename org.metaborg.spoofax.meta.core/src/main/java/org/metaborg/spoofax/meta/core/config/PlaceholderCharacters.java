package org.metaborg.spoofax.meta.core.config;

public class PlaceholderCharacters {
    public String prefix;
    public String suffix;


    public PlaceholderCharacters(String prefix, String postfix) {
        if (prefix == null) throw new IllegalArgumentException();
        this.prefix = prefix;
        this.suffix = postfix;
    }


    @Override public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + suffix.hashCode();
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
        final PlaceholderCharacters other = (PlaceholderCharacters) obj;
        if(!suffix.equals(other.suffix))
            return false;
        if(!prefix.equals(other.prefix))
            return false;
        return true;
    }

    @Override public String toString() {
        return prefix + "PLHDR" + (suffix == null ? "" : suffix);
    }
}
