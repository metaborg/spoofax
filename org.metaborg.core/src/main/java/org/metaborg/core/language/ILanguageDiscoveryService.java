package org.metaborg.core.language;

import java.util.Set;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.MetaborgException;

/**
 * Interface for a language discovery service that finds and loads language components and implementations.
 */
public interface ILanguageDiscoveryService {
    /**
     * Load a language implementation from a directory that contains a compiled language component.
     * 
     * @param directory
     *            Directory that contains the compiled language component.
     * @return Loaded language implementation.
     * @throws MetaborgException
     *             When loading the language component fails, or when more than one language implementation is loaded.
     */
    ILanguageImpl languageFromDirectory(FileObject directory) throws MetaborgException;

    /**
     * Load language implementations from a directory that contains a compiled language component.
     * 
     * @param directory
     *            Directory that contains the compiled language component.
     * @return Loaded language implementations.
     * @throws MetaborgException
     *             When loading the language component fails.
     */
    Set<ILanguageImpl> languagesFromDirectory(FileObject directory) throws MetaborgException;


    /**
     * Load a language implementation from a ZIP archive file that contains a compiled language component.
     * 
     * @param archiveFile
     *            ZIP archive file that contains the compiled language component.
     * @return Loaded language implementation.
     * @throws MetaborgException
     *             When loading the language component fails, or when more than one language implementation is loaded.
     */
    ILanguageImpl languageFromArchive(FileObject archiveFile) throws MetaborgException;

    /**
     * Load language implementations from a ZIP archive file that contains a compiled language component.
     * 
     * @param archiveFile
     *            ZIP archive file that contains the compiled language component.
     * @return Loaded language implementations.
     * @throws MetaborgException
     *             When loading the language component fails.
     */
    Set<ILanguageImpl> languagesFromArchive(FileObject archiveFile) throws MetaborgException;


    /**
     * Scans given directory and all descendants for language components and loads them all.
     * 
     * @param directory
     *            Directory to start scanning for language components.
     * @return Loaded language implementations.
     * @throws MetaborgException
     *             When <code>directory</code> is not a directory or does not exist, when loading a language component
     *             fails, or when an unexpected I/O error occurs.
     */
    Set<ILanguageImpl> scanLanguagesInDirectory(FileObject directory) throws MetaborgException;


    /**
     * Load a language component from a directory that contains a compiled language component.
     * 
     * @param directory
     *            Directory that contains the compiled language component.
     * @return Loaded language component.
     * @throws MetaborgException
     *             When loading the language component fails.
     */
    ILanguageComponent componentFromDirectory(FileObject directory) throws MetaborgException;

    /**
     * Load a language component from a ZIP archive file that contains a compiled language component.
     * 
     * @param archiveFile
     *            ZIP archive file that contains the compiled language component.
     * @return Loaded language component.
     * @throws MetaborgException
     *             When loading the language component fails.
     */
    ILanguageComponent componentFromArchive(FileObject archiveFile) throws MetaborgException;

    /**
     * Scans given directory and all descendants for language components and loads them all.
     * 
     * @param directory
     *            Directory to start scanning for language components.
     * @return Loaded language components.
     * @throws MetaborgException
     *             When <code>directory</code> is not a directory or does not exist, when loading a language component
     *             fails, or when an unexpected I/O error occurs.
     */
    Set<ILanguageComponent> scanComponentsInDirectory(FileObject directory) throws MetaborgException;


    /**
     * Request language discovery at given location. Returns language discovery requests which can be checked for
     * availability and errors. Pass requests to {@link #discover(ILanguageDiscoveryRequest)} to actually load the
     * components.
     * 
     * @param location
     *            The directory to search in.
     * @return Language discovery requests. Empty when no components can be discovered.
     * @throws MetaborgException
     *             When searching for language components fails unexpectedly.
     * @deprecated Use {@link ILanguageComponentFactory#requestFromDirectory} or
     *             {@link ILanguageComponentFactory#requestFromArchive} for creating requests, or non-deprecated methods
     *             from this interface.
     */
    @Deprecated Iterable<ILanguageDiscoveryRequest> request(FileObject location) throws MetaborgException;

    /**
     * Discover language component with given request.
     * 
     * @param request
     *            Language discovery request created by {@link #request(FileObject)}.
     * @return Language components that were created.
     * @throws MetaborgException
     *             When discovery fails unexpectedly.
     * @deprecated Use {@link ILanguageComponentFactory#createConfig} and {@link ILanguageService#add}, or
     *             non-deprecated methods from this interface.
     */
    @Deprecated ILanguageComponent discover(ILanguageDiscoveryRequest request) throws MetaborgException;

    /**
     * Discover all language components with the given requests.
     * 
     * @param requests
     *            Language discovery requests created by {@link #request(FileObject)}.
     * @return Language components that were created.
     * @throws MetaborgException
     *             When discovery fails unexpectedly.
     * @deprecated Use {@link ILanguageComponentFactory#createConfig} and {@link ILanguageService#add}, or
     *             non-deprecated methods from this interface.
     */
    @Deprecated Iterable<ILanguageComponent> discover(Iterable<ILanguageDiscoveryRequest> requests)
        throws MetaborgException;
}
