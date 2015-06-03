package org.metaborg.spoofax.core.language;

public class LanguageId {
    public final String name;
    public final LanguageVersion version;


    public LanguageId(String name, LanguageVersion version) {
        this.name = name;
        this.version = version;
    }


    @Override public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + name.hashCode();
        result = prime * result + version.hashCode();
        return result;
    }

    @Override public boolean equals(Object obj) {
        if(this == obj)
            return true;
        if(obj == null)
            return false;
        if(getClass() != obj.getClass())
            return false;
        final LanguageId other = (LanguageId) obj;
        if(!name.equals(other.name))
            return false;
        if(!version.equals(other.version))
            return false;
        return true;
    }

    @Override public String toString() {
        return String.format("LanguageId [name=%s, version=%s]", name, version);
    }
}
