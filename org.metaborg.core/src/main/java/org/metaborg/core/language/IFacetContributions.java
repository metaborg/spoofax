package org.metaborg.core.language;

import javax.annotation.Nullable;

import org.metaborg.core.MetaborgRuntimeException;

import com.google.common.collect.Iterables;

/**
 * Interface representing language facet contributions. Facets are retrieved by type, multiple facets of the same type
 * are allowed. Clients determine how multiple facets of the same type are handled.
 */
public interface IFacetContributions {
    /**
     * Checks if there is at least one facet of given type.
     * 
     * @param type
     *            Facet type.
     * @return True if at least one facet of given type exists, false otherwise.
     */
    boolean hasFacet(Class<? extends IFacet> type);

    /**
     * Returns facets of given type.
     * 
     * @param type
     *            Facet type
     * @return Facets of given type.
     */
    <T extends IFacet> Iterable<T> facets(Class<T> type);

    /**
     * Returns facet contributions of given type.
     * 
     * @param type
     *            Facet type
     * @return Facet contributions of given type.
     */
    <T extends IFacet> Iterable<FacetContribution<T>> facetContributions(Class<T> type);

    /**
     * Returns a facet of given type.
     * 
     * @param type
     *            Facet type.
     * @return Facet of given type, or null if there is no facet of given type.
     * @throws MetaborgRuntimeException
     *             When there are multiple facets of given type.
     */
    default @Nullable <T extends IFacet> T facet(Class<T> type) {
        // GTODO: code duplication with LanguageComponent, use default interface implementation in Java 8.
        final Iterable<T> facets = facets(type);
        final int size = Iterables.size(facets);
        if(size == 0) {
            return null;
        } else if(size > 1) {
            throw new MetaborgRuntimeException(
                "Multiple facets of type " + type + " found, while only a single facet is supported");
        }
        return Iterables.get(facets, 0);
    }

    /**
     * Returns a facet contribution of given type.
     * 
     * @param type
     *            Facet type.
     * @return Facet contribution of given type.
     * @throws MetaborgRuntimeException
     *             When there are multiple facets of given type.
     */
    default @Nullable <T extends IFacet> FacetContribution<T> facetContribution(Class<T> type) {
        // GTODO: code duplication with LanguageComponent, use default interface implementation in Java 8.
        final Iterable<FacetContribution<T>> facetContributions = facetContributions(type);
        final int size = Iterables.size(facetContributions);
        if(size == 0) {
            return null;
        } else if(size > 1) {
            throw new MetaborgRuntimeException(
                "Multiple facets of type " + type + " found, while only a single facet is supported");
        }
        return Iterables.get(facetContributions, 0);
    }

    /**
     * @return All facets.
     */
    Iterable<IFacet> facets();

    /**
     * @return All facet contributions.
     */
    Iterable<FacetContribution<IFacet>> facetContributions();
}
