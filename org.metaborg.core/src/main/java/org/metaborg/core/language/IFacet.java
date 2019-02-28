package org.metaborg.core.language;

/**
 * Interface that represents a facet of a language.
 */
public interface IFacet {
    /**
     * @return a unique key object per Facet implementation
     */
    default Class<? extends IFacet> getKey() {
        return this.getClass();
    }
}
