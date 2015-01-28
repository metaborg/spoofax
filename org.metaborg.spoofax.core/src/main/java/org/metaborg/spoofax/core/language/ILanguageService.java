package org.metaborg.spoofax.core.language;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;

import rx.Observable;

/**
 * Interface for a language service that creates and destroys languages, maps names to active
 * languages, and provides an observable of language changes.
 */
public interface ILanguageService {
    /**
     * Returns the active language for given language name. A language is active if it has the
     * highest version, and highest loading date.
     * 
     * @param name
     *            Name of the language.
     * @return Active language for given name, or null if there is none.
     */
    public @Nullable ILanguage get(String name);

    /**
     * Returns the language at given location.
     * 
     * @param location
     *            Location of the language.
     * @return Language at given location, or null if it does not exist.
     */
    public @Nullable ILanguage get(FileName location);
    
    /**
     * Returns the language with given name, version, and location.
     * 
     * @param name
     *            Name of the language.
     * @param version
     *            Version of the language.
     * @param location
     *            Location of the language.
     * @return Language with given name, version, and location, or null if it does not exist.
     */
    public @Nullable ILanguage get(String name, LanguageVersion version, FileObject location);

    /**
     * Returns all languages
     * 
     * @return Iterable over all languages.
     */
    public Iterable<ILanguage> getAll();

    /**
     * Returns all active languages.
     * 
     * @return Iterable over all active languages.
     */
    public Iterable<ILanguage> getAllActive();

    /**
     * Returns all languages with given name.
     * 
     * @param name
     *            Name of the languages.
     * @return Iterable over all languages with given name.
     */
    public Iterable<ILanguage> getAll(String name);

    /**
     * Returns all languages with given name and version.
     * 
     * @param name
     *            Name of the languages.
     * @param version
     *            Version of the langauges,
     * @return Iterable over all languages with given name and version.
     */
    public Iterable<ILanguage> getAll(String name, LanguageVersion version);

    /**
     * Returns an observable over language loaded, unloaded, activated, and deactivated changes.
     * 
     * @return Observable over language changes.
     */
    public Observable<LanguageChange> changes();

    /**
     * Creates a new language with given name, version, and location, that handles given extensions.
     * Automatically creates facets for the language from any {@link ILanguageFacetFactory}
     * implementations.
     * 
     * @param name
     *            Name of the language.
     * @param version
     *            Version of the language.
     * @param location
     *            Location of the language.
     * @return Created language
     * @throws IllegalStateException
     *             when given location does not exist, or if it is not possible to determine if the
     *             location exists.
     * @throws IllegalStateException
     *             when a language with a different name or version has already been created at
     *             given location.
     * @throws IllegalStateException
     *             when automatically creating facets fails unexpectedly.
     */
    public ILanguage create(String name, LanguageVersion version, FileObject location);

    /**
     * Destroys given language
     * 
     * @param language
     *            The language to destroy.
     * @throws IllegalStateException
     *             when language does not exist or has already been destroyed.
     */
    public void destroy(ILanguage language);
}
