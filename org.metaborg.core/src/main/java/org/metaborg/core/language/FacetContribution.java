package org.metaborg.core.language;

public class FacetContribution<T extends IFacet> {
    public final T facet;
    public final ILanguageComponent contributor;


    public FacetContribution(T facet, ILanguageComponent contributor) {
        this.facet = facet;
        this.contributor = contributor;
    }
}
