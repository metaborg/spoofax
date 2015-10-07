package org.metaborg.core.language;

import org.apache.commons.vfs2.FileObject;

/**
 * Interface that represents a language component. A language component contributes facets to language implementations.
 * Facet operations only return facets of this component.
 */
public interface ILanguageComponent extends IFacetContributions {
    /**
     * @return Identifier of this component.
     */
    public abstract LanguageIdentifier id();

    /**
     * @return Location of this component.
     */
    public abstract FileObject location();

    /**
     * @return Sequence identifier of this component. Used to find out if a component was created after or before
     *         another component.
     */
    public abstract int sequenceId();

    /**
     * @return All language implementations that this component contributes to.
     */
    public abstract Iterable<? extends ILanguageImpl> contributesTo();
}
