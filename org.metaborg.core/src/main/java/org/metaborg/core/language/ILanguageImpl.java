package org.metaborg.core.language;

import org.apache.commons.vfs2.FileObject;

/**
 * Interface that represents a language implementation. A language implementation is a view over all components that
 * contribute to the implementation. An implementation belongs to a language. Facet operations return facets from all
 * components.
 */
public interface ILanguageImpl extends IFacetContributions {
    /**
     * @return Identifier of this implementation.
     */
    LanguageIdentifier id();

    /**
     * @return Maximum sequence identifier of this implementation's components.
     */
    int sequenceId();

    /**
     * @return Locations of this implementation's components.
     */
    Iterable<FileObject> locations();

    /**
     * @return All components that contribute to this implementation.
     */
    Iterable<ILanguageComponent> components();

    /**
     * @return Language this implementation belongs to.
     */
    ILanguage belongsTo();
}
