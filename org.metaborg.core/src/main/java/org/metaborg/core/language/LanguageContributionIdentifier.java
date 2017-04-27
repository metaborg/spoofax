package org.metaborg.core.language;

import java.io.Serializable;
import java.util.Objects;

public class LanguageContributionIdentifier implements Serializable {
    private static final long serialVersionUID = -3074869698162405693L;

    /**
     * Identifier of the language implementation to contribute to.
     */
    public final LanguageIdentifier id;
    public final String name;


    public LanguageContributionIdentifier(LanguageIdentifier id, String name) {
        this.id = id;
        this.name = name;
    }


    @Override public int hashCode() {
        return Objects.hash(id, name);
    }

    @Override public boolean equals(Object obj) {
        if(this == obj)
            return true;
        if(obj == null)
            return false;
        if(getClass() != obj.getClass())
            return false;
        LanguageContributionIdentifier other = (LanguageContributionIdentifier) obj;
        if(!id.equals(other.id))
            return false;
        if(!name.equals(other.name))
            return false;
        return true;
    }

    @Override public String toString() {
        return "contribution to " + id + " of language " + name;
    }

}