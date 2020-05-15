package org.metaborg.spoofax.core.terms;

import javax.annotation.Nullable;

import org.metaborg.core.language.ILanguageComponent;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.project.IProject;
import org.spoofax.interpreter.terms.ITermFactory;

/**
 * Interface for retrieving term factories for generic use.
 */
public interface ITermFactoryService {
    /**
     * Returns the generic term factory.
     * 
     * @return Generic term factory.
     */
    ITermFactory getGeneric();
}
