package org.metaborg.spoofax.core.language;

public class LanguageFacetChange {
    public enum Kind {
        ADDED, REMOVED
    }


    public final ILanguageFacet facet;
    public final Kind kind;


    public LanguageFacetChange(ILanguageFacet facet, Kind kind) {
        this.kind = kind;
        this.facet = facet;
    }
}
