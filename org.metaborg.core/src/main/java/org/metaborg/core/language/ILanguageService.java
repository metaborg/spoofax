package org.metaborg.core.language;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;

import rx.Observable;

/**
 * Interface for a language service that creates and destroys languages, maps names to active languages, and provides an
 * observable of language changes. A language is active if it has the highest version, and highest loading date.
 */
public interface ILanguageService {
    /**
     * Returns the active language with a given id and version.
     * 
     * @param identifier
     *            Identifier of the language.
     * @return Language with given identifier, or null if it does not exist.
     */
    public @Nullable ILanguage get(LanguageIdentifier identifier);

    /**
     * Returns the active language with a given group id and id.
     * 
     * @param groupId
     *            Group id of the language.
     * @param id
     *            Id of the language.
     * @return Language with given group id and id, or null if it does not exist.
     */
    public @Nullable ILanguage get(String groupId, String id);

    /**
     * Returns the active language for given language name.
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
     * Returns all languages with given identifier and version.
     * 
     * @param identifier
     *            Identifier of the language.
     * @return Iterable over all languages with given identifier and version.
     */
    public Iterable<ILanguage> getAll(LanguageIdentifier identifier);

    /**
     * Returns all languages with given name.
     * 
     * @param name
     *            Name of the languages.
     * @return Iterable over all languages with given name.
     */
    public Iterable<ILanguage> getAll(String name);

    /**
     * Returns all active languages.
     * 
     * @return Iterable over all active languages.
     */
    public Iterable<ILanguage> getAllActive();

    /**
     * Returns all languages
     * 
     * @return Iterable over all languages.
     */
    public Iterable<ILanguage> getAll();


    /**
     * Returns an observable over language loaded, unloaded, activated, and deactivated changes.
     * 
     * @return Observable over language changes.
     */
    public Observable<LanguageChange> changes();


    /**
     * Creates a new empty language with given identifier, location, and name.
     * 
     * @param identifier
     *            Identifier of the language.
     * @param location
     *            Location of the language.
     * @param name
     *            Name of the language.
     * 
     * @return Created language
     */
    public ILanguage create(LanguageIdentifier identifier, FileObject location, String name);

    /**
     * Adds given language.
     * 
     * @param language
     *            Language to add.
     * @throws IllegalStateException
     *             when given language's location does not exist, or if it is not possible to determine if the location
     *             exists.
     * @throws IllegalStateException
     *             when a language with a different name or version has already been created at given language's
     *             location.
     */
    public void add(ILanguage language);

    /**
     * Removes given language.
     * 
     * @param language
     *            Language to remove.
     * @throws IllegalStateException
     *             when language does not exist or has already been removed.
     */
    public void remove(ILanguage language);
}
