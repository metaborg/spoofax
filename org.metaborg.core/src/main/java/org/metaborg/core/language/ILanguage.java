package org.metaborg.core.language;

import javax.annotation.Nullable;


/**
 * Interface that represents a language. A language has multiple implementations.
 */
public interface ILanguage {
    /**
     * @return Name of this language.
     */
    String name();

    /**
     * @return All language implementations that belong to this language.
     */
    Iterable<? extends ILanguageImpl> impls();

    /**
     * @return Active language implementation for this language. A language implementation is active when it has a
     *         higher version number than other language implementations. When there are multiple implementations with
     *         the highest version number, the one that was added last wins. Returns null when {@code #impls()} returns
     *         an empty iterable, which only happens if this language has been removed from the language service.
     */
    @Nullable ILanguageImpl activeImpl();
}