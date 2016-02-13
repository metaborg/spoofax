package org.metaborg.core.language;

/**
 * Interface for services that cache language resources, to support invalidation of those caches when the language is
 * removed or reloaded. Add Guice bindings to this interface for classes that implement it.
 */
public interface ILanguageCache {
    /**
     * Invalidate the cache for given language component.
     * 
     * @param component
     *            Component to invalidate the cache for.
     */
    void invalidateCache(ILanguageComponent component);

    /**
     * Invalidate the cache for given language implementation.
     * 
     * @param impl
     *            Implementation to invalidate the cache for.
     */
    void invalidateCache(ILanguageImpl impl);
}
