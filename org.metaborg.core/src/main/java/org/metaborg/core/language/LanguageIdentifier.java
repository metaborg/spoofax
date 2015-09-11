package org.metaborg.core.language;

import java.io.Serializable;

public class LanguageIdentifier implements Serializable {
    private static final long serialVersionUID = 8892997161544718124L;
    
    public final String groupId;
    public final String id;
    public final LanguageVersion version;


    public LanguageIdentifier(String groupId, String id, LanguageVersion version) {
        this.groupId = groupId;
        this.id = id;
        this.version = version;
    }

    public LanguageIdentifier(LanguageIdentifier identifier, LanguageVersion version) {
        this(identifier.groupId, identifier.id, version);
    }


    @Override public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + groupId.hashCode();
        result = prime * result + id.hashCode();
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
        final LanguageIdentifier other = (LanguageIdentifier) obj;
        if(!groupId.equals(other.groupId))
            return false;
        if(!id.equals(other.id))
            return false;
        if(!version.equals(other.version))
            return false;
        return true;
    }

    @Override public String toString() {
        return groupId + ":" + id + "-" + version;
    }
}
