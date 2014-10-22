package org.metaborg.spoofax.core.language;

import java.util.Map;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.spoofax.core.service.actions.Action;
import org.metaborg.spoofax.core.service.actions.ActionsFacet;
import org.metaborg.spoofax.core.service.stratego.StrategoFacet;
import org.metaborg.spoofax.core.service.syntax.SyntaxFacet;

import com.google.common.collect.ImmutableSet;

/**
 * Interface for a language discovery service that finds and creates all languages found at a certain location.
 */
public interface ILanguageDiscoveryService {
    /**
     * Discover and create all languages at given location.
     * 
     * @param location
     *            The directory to search in.
     * @return An iterable over all languages that were discovered and created.
     * @throws IllegalStateException
     *             when {@link ILanguageService} throws when creating a language.
     */
    public Iterable<ILanguage> discover(FileObject location) throws Exception;

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
}
