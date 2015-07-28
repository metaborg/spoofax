package org.metaborg.core.language;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;

import rx.Observable;

/**
 * Interface that represents a language and its facets. Implementors implement {@link #hashCode()}, and
 * {@link #equals(Object)} using {@link #name()}, {@link #version()}, and {@link #location()}.
 */
public interface ILanguage {
    /**
     * Returns the identifier of the language.
     */
    public LanguageIdentifier id();

    /**
     * Returns the location of the language.
     */
    public FileObject location();

    /**
     * Returns the name of the language.
     */
    public String name();

    /**
     * Returns the sequence identifier of the language. Used to find out if a language was created after or before
     * another language.
     */
    public int sequenceId();



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
     * @param facet
     *            The facet to add.
     * @return The added facet
     * @throws IllegalStateException
     *             when facet with given type already exists in the language.
     */
    public <T extends ILanguageFacet> ILanguageFacet addFacet(T facet);

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


    /* Hint for hashCode implementation. */
    public abstract int hashCode();

    /* Hint for equals implementation. */
    public abstract boolean equals(Object other);
}
