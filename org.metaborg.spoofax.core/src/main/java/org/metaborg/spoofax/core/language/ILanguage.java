package org.metaborg.spoofax.core.language;

import java.util.Date;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileName;

import rx.Observable;

/**
 * Interface that represents a language and its facets.
 */
public interface ILanguage extends Comparable<ILanguage> {
    /**
     * Returns the name of the language.
     */
    public String name();

    /**
     * Returns the version of the language.
     */
    public LanguageVersion version();

    /**
     * Returns the location of the language.
     */
    public FileName location();

    /**
     * Returns the extensions that this language handles.
     */
    public Iterable<String> extensions();

    /**
     * Query if language handles given extension.
     * 
     * @param extension
     *            Extension to query.
     * @return True if language handles given extension, false otherwise.
     */
    public boolean hasExtension(String extension);

    /**
     * Returns the date at when the language was created.
     */
    public Date createdDate();


    /**
     * Returns the facets of this language.
     * 
     * @return Iterable over the facets of this language.
     */
    public Iterable<ILanguageFacet> facets();

    /**
     * Returns facet of given type.
     * 
     * @param type
     *            Facet type
     * @return Facet of given type, or null if it does not exist.
     */
    public @Nullable <T extends ILanguageFacet> T facet(Class<T> type);

    /**
     * Returns an observable over facet added and removed changes.
     * 
     * @return Observable over facet changes.
     */
    public Observable<LanguageFacetChange> facetChanges();

    /**
     * Adds given facet to the language.
     * 
     * @param type
     *            Type of the facet to add.
     * @param facet
     *            The facet to add.
     * @return The added facet
     * @throws IllegalStateException
     *             when facet with given type already exists in the language.
     */
    public <T extends ILanguageFacet> ILanguageFacet addFacet(Class<T> type, T facet);

    /**
     * Removes facet of given type from the language.
     * 
     * @param type
     *            Type of the facet to remove.
     * @return The removed facet
     * @throws IllegalStateException
     *             when facet with given type does not exist in the language.
     */
    public <T extends ILanguageFacet> ILanguageFacet removeFacet(Class<T> type);
}
