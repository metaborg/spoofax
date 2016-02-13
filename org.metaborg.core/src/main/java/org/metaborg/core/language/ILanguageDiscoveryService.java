package org.metaborg.core.language;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.MetaborgException;

/**
 * Interface for a language discovery service that finds and adds all language components,
 * at a given location, to the language service.
 */
public interface ILanguageDiscoveryService {

    /**
     * Request language discovery at given location. Returns language discovery requests which can be checked for
     * availability and errors. Pass requests to {@link #discover(ILanguageDiscoveryRequest)} to actually load the
     * components.
     * 
     * @param location The directory to search in.
     * @return Language discovery requests. Empty when no components can be discovered.
     * @throws MetaborgException
     *             When searching for language components fails unexpectedly.
     */
    Iterable<ILanguageDiscoveryRequest> request(FileObject location) throws MetaborgException;

    /**
     * Discover language component with given request.
     * 
     * @param request Language discovery request created by {@link #request(FileObject)}.
     * @return Language components that were created.
     * @throws MetaborgException
     *             When discovery fails unexpectedly.
     */
    ILanguageComponent discover(ILanguageDiscoveryRequest request) throws MetaborgException;

    /**
     * Discover all language components with the given requests.
     * 
     * @param requests Language discovery requests created by {@link #request(FileObject)}.
     * @return Language components that were created.
     * @throws MetaborgException
     *             When discovery fails unexpectedly.
     */
    Iterable<ILanguageComponent> discover(Iterable<ILanguageDiscoveryRequest> requests) throws MetaborgException;

}
