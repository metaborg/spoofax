package org.metaborg.core.language;

/**
 * Interface that represents a language. A language has multiple implementations.
 */
public interface ILanguage {
    /**
     * @return Name of this language.
     */
    public abstract String name();

    /**
     * @return All language implementations that belong to this language.
     */
    public abstract Iterable<ILanguageImpl> all();
}
