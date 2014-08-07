package org.metaborg.spoofax.core.language;

import org.metaborg.spoofax.core.SpoofaxModule;

/**
 * Interface representing a factory that creates {@link ILanguageFacet} instances. Usage requires implementation of the
 * interface, and registration of the interface in {@link SpoofaxModule} as follows:
 * 
 * <code>
 * facetFactoriesBinder.addBinding().to(Implementation.class);
 * </code>
 */
public interface ILanguageFacetFactory {
    /**
     * Create zero, one, or many language facets and add them to the given language.
     * 
     * @param language
     *            The language to create facets for.
     * @throws Exception
     *             when creating facets unexpectedly fails.
     */
    public void create(ILanguage language) throws Exception;
}
