package org.metaborg.core.language;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.MetaborgException;

/**
 * Interface for a language discovery service that finds and adds all language components,
 * at a given location, to the language service.
 */
public interface INewLanguageDiscoveryService {

    /**
     * Request language discovery at given location. Returns language discovery requests which can be checked for
     * availability and errors. Pass requests to {@link #discover(INewLanguageDiscoveryRequest)} to actually load the
     * components.
     * 
     * @param location The directory to search in.
     * @return Language discovery requests. Empty when no components can be discovered.
     * @throws MetaborgException
     *             When searching for language components fails unexpectedly.
     */
    Iterable<INewLanguageDiscoveryRequest> request(FileObject location) throws MetaborgException;

    /**
     * Discover language component with given request.
     * 
     * @param request Language discovery request created by {@link #request(FileObject)}.
     * @return Language components that were created.
     * @throws MetaborgException
     *             When discovery fails unexpectedly.
     */
    ILanguageComponent discover(INewLanguageDiscoveryRequest request) throws MetaborgException;

    /**
     * Discover all language components with the given requests.
     * 
     * @param requests Language discovery requests created by {@link #request(FileObject)}.
     * @return Language components that were created.
     * @throws MetaborgException
     *             When discovery fails unexpectedly.
     */
    Iterable<ILanguageComponent> discover(Iterable<INewLanguageDiscoveryRequest> requests) throws MetaborgException;

    /**
     * Discover and create all language components at given location.
     * 
     * @param location
     *            The directory to search in.
     * @return Language components that were created.
     * @throws MetaborgException
     *             When discovery fails unexpectedly.
     * @deprecated Use {@link #request(FileObject)} and {@link #discover(Iterable)} instead.
     */
    @Deprecated
    Iterable<ILanguageComponent> discover(FileObject location) throws MetaborgException;
}
