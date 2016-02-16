package org.metaborg.core.language;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.project.ILanguageSpec;

/**
 * Interface for identifying the language of a resource.
 */
public interface ILanguageIdentifierService {
    /**
     * Checks if given resource is of given language.
     * 
     * @param resource
     *            Resource to check.
     * @param language
     *            Language to check against.
     * @return True if resource is of given language, false otherwise.
     */
    boolean identify(FileObject resource, ILanguageImpl language);

    /**
     * Attempts to identify the active language of given resource.
     * 
     * @param resource
     *            Resource to identify.
     * @return Identified language, or null if language could not be identified.
     * @throws IllegalStateException
     *             When a resource can be identified to languages with different names.
     */
    @Nullable ILanguageImpl identify(FileObject resource);

    /**
     * Attempts to identify the active language of given resource.
     *
     * @param resource
     *            Resource to identify.
     * @param languageSpec
     *            The language specification to which the resource belongs; or <code>null</code> if not known.
     * @return Identified language, or null if language could not be identified.
     * @throws IllegalStateException
     *             When a resource can be identified to languages with different names.
     */
    @Nullable ILanguageImpl identify(FileObject resource, @Nullable ILanguageSpec languageSpec);

    /**
     * Attempts to identify the active language of given resource, and return an identified resource.
     * 
     * @param resource
     *            Resource to identify.
     * @return Identified resource, or null if language could not be identified.
     * @throws IllegalStateException
     *             When a resource can be identified to languages with different names.
     */
    @Nullable IdentifiedResource identifyToResource(FileObject resource);

    /**
     * Attempts to identify the active language of given resource, and return an identified resource.
     *
     * @param resource
     *            Resource to identify.
     * @param languageSpec
     *            The language specification to which the resource belongs; or <code>null</code> if not known.
     * @return Identified resource, or null if language could not be identified.
     * @throws IllegalStateException
     *             When a resource can be identified to languages with different names.
     */
    @Nullable IdentifiedResource identifyToResource(FileObject resource, @Nullable ILanguageSpec languageSpec);

    /**
     * Attempts to identify the language of given resource, among given list of languages.
     * 
     * @param resource
     *            Resource to identify.
     * @return Identified language, or null if language could not be identified.
     * @throws IllegalStateException
     *             When a resource can be identified to multiple languages.
     */
    @Nullable ILanguageImpl identify(FileObject resource, Iterable<? extends ILanguageImpl> languages);

    /**
     * Attempts to identify the language of given resource, among given list of languages, and return an identified
     * resource.
     * 
     * @param resource
     *            Resource to identify.
     * @return Identified resource, or null if language could not be identified.
     * @throws IllegalStateException
     *             When a resource can be identified to multiple languages.
     */
    @Nullable IdentifiedResource identifyToResource(FileObject resource,
                                                    Iterable<? extends ILanguageImpl> languages);

    /**
     * Returns if language identification is available for given implementation.
     * 
     * @param impl
     *            Language implementation to check.
     * @return True if identification is available, false if not.
     */
    boolean available(ILanguageImpl impl);
}
