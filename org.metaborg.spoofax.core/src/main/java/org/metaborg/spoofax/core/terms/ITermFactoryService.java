package org.metaborg.spoofax.core.terms;

import org.metaborg.core.language.ILanguage;
import org.spoofax.interpreter.terms.ITermFactory;

/**
 * Interface for retrieving term factories for languages and generic use.
 */
public interface ITermFactoryService {
    /**
     * Return the term factory to be used when constructing new terms for given language.
     * 
     * @param language
     *            Language to get the term factory for.
     * @return Language-specific term factory.
     */
    public ITermFactory get(ILanguage language);

    /**
     * Returns the generic term factory.
     * 
     * @return Generic term factory.
     */
    public ITermFactory getGeneric();
}
