package org.metaborg.core.language;

import org.apache.commons.vfs2.FileObject;

/**
 * Interface for a language discovery service that finds, and adds all language components found at a certain location
 * to the language service.
 */
public interface ILanguageDiscoveryService {
    /**
     * Discover and create all language components at given location.
     * 
     * @param location
     *            The directory to search in.
     * @return Language components that were discovered and created.
     * @throws IllegalStateException
     *             When {@link ILanguageService} throws when adding a language.
     */
    public Iterable<ILanguageComponent> discover(FileObject location) throws Exception;
}
