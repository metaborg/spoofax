package org.metaborg.core.language;

import org.apache.commons.vfs2.FileObject;

/**
 * Interface for a language discovery service that finds, creates, and adds all languages found at a certain location.
 */
public interface ILanguageDiscoveryService {
    /**
     * Discover and create all languages at given location.
     * 
     * @param location
     *            The directory to search in.
     * @return An iterable over all languages that were discovered and created.
     * @throws IllegalStateException
     *             when {@link ILanguageService} throws when adding a language.
     */
    public Iterable<ILanguage> discover(FileObject location) throws Exception;
}
