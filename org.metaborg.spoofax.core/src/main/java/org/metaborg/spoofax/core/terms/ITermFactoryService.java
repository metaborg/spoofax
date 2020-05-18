package org.metaborg.spoofax.core.terms;

import javax.annotation.Nullable;

import org.metaborg.core.language.ILanguageComponent;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.project.IProject;
import org.spoofax.interpreter.terms.ITermFactory;

/**
 * Interface for retrieving term factories for languages and generic use.
 */
public interface ITermFactoryService {
    /**
     * Return the term factory to be used when constructing new terms for given language implementation.
     * 
     * @param impl
     *            Implementation to get the term factory for.
     * @param project
     *            The current project.
     * @return Language-specific term factory.
     */
    ITermFactory get(ILanguageImpl impl, @Nullable IProject project);

    /**
     * Return the term factory to be used when constructing new terms for given language component.
     * 
     * @param component
     *            Component to get the term factory for.
     * @param project
     *            The current project.
     * @return Language-specific term factory.
     */
    ITermFactory get(ILanguageComponent component, @Nullable IProject project);

    /**
     * Returns the generic term factory.
     * 
     * @return Generic term factory.
     */
    ITermFactory getGeneric();
}
