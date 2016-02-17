package org.metaborg.core.language;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.project.config.ILanguageComponentConfig;

/**
 * Interface that represents a language component. A language component contributes facets to language implementations.
 * Facet operations only return facets of this component.
 */
public interface ILanguageComponent extends IFacetContributions {
    /**
     * @return Identifier of this component.
     */
    LanguageIdentifier id();

    /**
     * @return Location of this component.
     */
    FileObject location();

    /**
     * @return Sequence identifier of this component. Used to find out if a component was created after or before
     *         another component.
     */
    int sequenceId();

    /**
     * @return All language implementations that this component contributes to.
     */
    Iterable<? extends ILanguageImpl> contributesTo();
    
    /**
     * @return Configuration of this component.
     */
    ILanguageComponentConfig config();
}
