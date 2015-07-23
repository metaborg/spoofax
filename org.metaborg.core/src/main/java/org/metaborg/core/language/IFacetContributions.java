package org.metaborg.core.language;

import javax.annotation.Nullable;

import org.metaborg.core.MetaborgRuntimeException;

/**
 * Interface representing language facet contributions. Facets are retrieved by type, multiple facets of the same type
 * are allowed. It is implementation-specific how multiple facets of the same type are handled.
 */
public interface IFacetContributions {
    /**
     * @return All facets.
     */
    public Iterable<IFacet> facets();

    /**
     * @return All facet contributions.
     */
    public Iterable<FacetContribution<IFacet>> facetContributions();

    /**
     * Returns facets of given type.
     * 
     * @param type
     *            Facet type
     * @return Facets of given type.
     */
    public <T extends IFacet> Iterable<T> facets(Class<T> type);

    /**
     * Returns facet contributions of given type.
     * 
     * @param type
     *            Facet type
     * @return Facet contributions of given type.
     */
    public <T extends IFacet> Iterable<FacetContribution<T>> facetContributions(Class<T> type);

    /**
     * Returns a facet of given type.
     * 
     * @param type
     *            Facet type
     * @return Facet of given type, or null if there is no facet of given type.
     * @throws MetaborgRuntimeException
     *             When there are multiple facets of given type.
     */
    public @Nullable <T extends IFacet> T facet(Class<T> type);
}
