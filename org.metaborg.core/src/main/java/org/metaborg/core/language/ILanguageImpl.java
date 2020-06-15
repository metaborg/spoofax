package org.metaborg.core.language;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.config.ILanguageImplConfig;

import java.util.List;
import java.util.Set;

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
    List<FileObject> locations();

    /**
     * @return All components that contribute to this implementation.
     */
    Set<ILanguageComponent> components();

    /**
     * @return Language this implementation belongs to.
     */
    ILanguage belongsTo();

    /**
     * @return Configuration of this language implementation.
     */
    ILanguageImplConfig config();
}
