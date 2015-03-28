package org.metaborg.spoofax.core.language;

/**
 * Interface for services that cache language resources, to support invalidation of those caches when the language is
 * removed or reloaded. Add Guice bindings to this interface for classes that implement it.
 */
public interface ILanguageCache {
    public abstract void invalidateCache(ILanguage language);
}
