package org.metaborg.spoofax.core.language;

import java.util.Map;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.spoofax.core.service.actions.Action;
import org.metaborg.spoofax.core.service.actions.ActionsFacet;
import org.metaborg.spoofax.core.service.stratego.StrategoFacet;
import org.metaborg.spoofax.core.service.syntax.SyntaxFacet;

import rx.Observable;

import com.google.common.collect.ImmutableSet;

/**
 * Interface for a language service that creates and destroys languages, maps names to active languages, and provides an
 * observable of language changes.
 */
public interface ILanguageService {
    /**
     * Returns the active language for given language name. A language is active if it has the highest version, and
     * highest loading date.
     * 
     * @param name
     *            Name of the language.
     * @return Active language for given name, or null if there is none.
     */
    public @Nullable ILanguage get(String name);

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
     * Returns the active language that handles given extension. A language is active if it has the highest version, and
     * highest loading date.
     * 
     * @param extension
     *            Extension that language handles.
     * @return Active language that handles given extension, or null if there is none.
     */
    public @Nullable ILanguage getByExt(String extension);

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
     * Returns all languages that handle given extension.
     * 
     * @param extension
     *            Extension that languages handle.
     * @return Iterable over all languages that handle given extension.
     */
    public Iterable<ILanguage> getAllByExt(String extension);

    /**
     * Returns a random language.
     * 
     * @return A random language, or null if no languages have been created.
     */
    public @Nullable ILanguage getAny();

    /**
     * Returns all supported extensions.
     * 
     * @return Iterable over all supported extensions.
     */
    public Iterable<String> getSupportedExt();

    /**
     * Returns an observable over language loaded, unloaded, activated, and deactivated changes.
     * 
     * @return Observable over language changes.
     */
    public Observable<LanguageChange> changes();

    /**
     * Creates a new language with given name, version, and location, that handles given extensions. Automatically
     * creates facets for the language from any {@link ILanguageFacetFactory} implementations.
     * 
     * @param name
     *            Name of the language.
     * @param version
     *            Version of the language.
     * @param location
     *            Location of the language.
     * @param extensions
     *            Extensions that language handles.
     * @return Created language
     * @throws IllegalStateException
     *             when given location does not exist, or if it is not possible to determine if the location exists.
     * @throws IllegalStateException
     *             when a language with a different name or version has already been created at given location.
     * @throws IllegalStateException
     *             when a language with a different name already handles any of given extensions.
     * @throws IllegalStateException
     *             when automatically creating facets fails unexpectedly.
     */
    public ILanguage create(String name, LanguageVersion version, FileObject location, ImmutableSet<String> extensions);


    /**
     * Creates a new language with given arguments. Automatically creates the {@link SyntaxFacet}, {@link StrategoFacet}
     * , and {@link ActionsFacet}, facets, plus facets from any {@link ILanguageFacetFactory} implementations.
     * 
     * @param name
     *            Name of the language.
     * @param version
     *            Version of the language.
     * @param location
     *            Location of the language.
     * @param extensions
     *            Extensions that language handles.
     * @param parseTable
     * @param startSymbol
     * @param ctreeFiles
     * @param jarFiles
     * @param strategoAnalysisStrategy
     * @param strategoOnSaveStrategy
     * @param actions
     * @return Created language
     * @throws IllegalStateException
     *             when given location does not exist, or if it is not possible to determine if the location exists.
     * @throws IllegalStateException
     *             when a language with a different name or version has already been created at given location.
     * @throws IllegalStateException
     *             when a language with a different name already handles any of given extensions.
     * @throws IllegalStateException
     *             when automatically creating facets fails unexpectedly.
     */
    public ILanguage create(String name, LanguageVersion version, FileObject location, ImmutableSet<String> extensions,
        FileObject parseTable, String startSymbol, ImmutableSet<FileObject> ctreeFiles,
        ImmutableSet<FileObject> jarFiles, @Nullable String strategoAnalysisStrategy,
        @Nullable String strategoOnSaveStrategy, Map<String, Action> actions);

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
