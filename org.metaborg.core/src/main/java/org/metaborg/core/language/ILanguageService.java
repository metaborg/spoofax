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
     * Gets a language component by its identifier.
     * 
     * @param identifier
     *            Identifier of the implementation to get.
     * @return Component with given identifier, or null if it could not be found.
     */
    public @Nullable ILanguageComponent getComponent(LanguageIdentifier identifier);

    /**
     * Gets a language component by its identifier;
     * or otherwise the baseline language component with the same identifier.
     *
     * @param identifier Identifier of the implementation to get.
     * @return Component with given identifier; or the baseline component with the
     * given identifier; or <code>null</code> if it could not be found.
     */
    @Nullable
    ILanguageComponent getComponentOrBaseline(LanguageIdentifier identifier);

    /**
     * Gets a language component by its location.
     * 
     * @param location
     *            Location of the implementation to get.
     * @return Component at given location, or null if it could not be found.
     */
    public @Nullable ILanguageComponent getComponent(FileName location);

    /**
     * Gets a language implementation by its identifier.
     * 
     * @param identifier
     *            Identifier of the implementation to get.
     * @return Implementation with given identifier, or null if it could not be found.
     */
    public @Nullable ILanguageImpl getImpl(LanguageIdentifier identifier);

    /**
     * Gets a language by its name.
     * 
     * @param name
     *            Name of the language to get.
     * @return Language with given name, or null if it could not be found.
     */
    public @Nullable ILanguage getLanguage(String name);


    /**
     * @return All language components.
     */
    public Iterable<? extends ILanguageComponent> getAllComponents();

    /**
     * @return All language implementations.
     */
    public Iterable<? extends ILanguageImpl> getAllImpls();

    /**
     * Gets language implementations with group id and id.
     * 
     * @param groupId
     *            Group ID of the implementations to get.
     * @param id
     *            ID of the implementations to get.
     * @return Implementations with given group id and id.
     */
    public abstract Iterable<? extends ILanguageImpl> getAllImpls(String groupId, String id);

    /**
     * @return All languages
     */
    public Iterable<? extends ILanguage> getAllLanguages();


    /**
     * @return Observable over language component changes.
     */
    public Observable<LanguageComponentChange> componentChanges();

    /**
     * @return Observable over language implementation changes.
     */
    public Observable<LanguageImplChange> implChanges();


    /**
     * Creates a request object with given identifier and location, contributing to given language implementation
     * identifiers. Returns a request object where facets can be added before passing it to
     * {@link #add(ILanguageComponent)}.
     * 
     * @param identifier
     *            Identifier of the component to create.
     * @param location
     *            Location of the component to create.
     * @param implIds
     *            Identifiers of language implementations that the component should contribute to.
     * 
     * @return Creation request object, when passed to {@link #add(ILanguageComponent)} actually adds the language.
     */
    public LanguageCreationRequest create(LanguageIdentifier identifier, FileObject location,
        Iterable<LanguageContributionIdentifier> implIds);

    /**
     * Adds language component created from given request object, and return the created component.
     * 
     * @param request
     *            Request object to process.
     * @return Created component.
     * @throws IllegalStateException
     *             When given component's location does not exist, or if it is not possible to determine if the location
     *             exists.
     * @throws IllegalStateException
     *             When a component with a different id has already been created at given component's location.
     */
    public ILanguageComponent add(LanguageCreationRequest request);

    /**
     * Removes given language component.
     * 
     * @param language
     *            Language to remove.
     * @throws IllegalStateException
     *             When component does not exist or has already been removed.
     */
    public void remove(ILanguageComponent component);
}
