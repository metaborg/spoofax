package org.metaborg.spoofax.core.language;

/**
 * Represents a change of a facet in a {@link ILanguage}.
 */
public class LanguageFacetChange {
    public enum Kind {
        ADD, REMOVE
    }


    public final ILanguageFacet facet;
    public final Kind kind;


    public LanguageFacetChange(ILanguageFacet facet, Kind kind) {
        this.kind = kind;
        this.facet = facet;
    }


    @Override public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + facet.hashCode();
        result = prime * result + kind.hashCode();
        return result;
    }

    @Override public boolean equals(Object obj) {
        if(this == obj)
            return true;
        if(obj == null)
            return false;
        if(getClass() != obj.getClass())
            return false;
        final LanguageFacetChange other = (LanguageFacetChange) obj;
        if(!facet.equals(other.facet))
            return false;
        if(kind != other.kind)
            return false;
        return true;
    }
}
