package org.metaborg.core.language;

import java.util.Collection;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.MetaborgException;

/**
 * Interface for creating {@link ComponentCreationConfig}s which are used to load language components into a
 * {@link ILanguageService}.
 */
public interface ILanguageComponentFactory {
    /**
     * Create a component creation configuration request from a directory that contains a compiled language component.
     * 
     * @param directory
     *            Directory that contains the compiled language component.
     * @return Request which can be checked for validity. Pass valid requests to
     *         {@link #createConfig(IComponentCreationConfigRequest)} to create a language component configuration.
     * @throws MetaborgException
     *             When <code>directory</code> is not a directory, does not exist, or does not contain a compiled
     *             language component; or when an unexpected I/O error occurs.
     */
    IComponentCreationConfigRequest requestFromDirectory(FileObject directory) throws MetaborgException;

    /**
     * Create a component creation configuration request from a ZIP archive file that contains a compiled language
     * component.
     * 
     * @param archiveFile
     *            ZIP archive file that contains the compiled language component.
     * @return Request which can be checked for validity. Pass valid requests to
     *         {@link #createConfig(IComponentCreationConfigRequest)} to create a language component configuration.
     * @throws MetaborgException
     *             When <code>archiveFile</code> is not a file, does not exist, or does not contain a compiled language
     *             component; or when an unexpected I/O error occurs.
     */
    IComponentCreationConfigRequest requestFromArchive(FileObject archiveFile) throws MetaborgException;

    /**
     * Creates component creation configuration requests by scanning given directory and all descendants for compiled
     * language components.
     * 
     * @param directory
     *            Directory to start scanning for compiled language components.
     * @return Requests which can be checked for validity. Pass valid requests to
     *         {@link #createConfig(IComponentCreationConfigRequest)} to create a language component configuration.
     * @throws MetaborgException
     *             When <code>directory</code> is not a directory, does not exist; or when an unexpected I/O error
     *             occurs.
     */
    Collection<IComponentCreationConfigRequest> requestAllInDirectory(FileObject directory) throws MetaborgException;


    /**
     * Creates a language component configuration from a valid request.
     * 
     * @param request
     *            Request to create a component configuration for.
     * @return A language component configuration which is passed to
     *         {@link ILanguageService#add(ComponentCreationConfig)} to load the component.
     * @throws MetaborgException
     *             When <code>request</code> is not valid, or when there are further configuration issues in the
     *             request.
     */
    ComponentCreationConfig createConfig(IComponentCreationConfigRequest request) throws MetaborgException;

    /**
     * Creates language component configurations from valid requests.
     * 
     * @param requests
     *            Requests to create component configurations for.
     * @return Language component configurations which are passed to
     *         {@link ILanguageService#add(ComponentCreationConfig)} to load the component.
     * @throws MetaborgException
     *             When a request is not valid, or when there are further configuration issues in a request.
     */
    Collection<ComponentCreationConfig> createConfigs(Iterable<IComponentCreationConfigRequest> requests)
        throws MetaborgException;
}
