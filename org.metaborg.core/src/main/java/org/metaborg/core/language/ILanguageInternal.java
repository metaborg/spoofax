package org.metaborg.core.language;

public interface ILanguageInternal extends ILanguage {
    /**
     * Adds a language implementation that belongs to this language.
     * 
     * @param implementation
     *            Language implementation to add.
     */
    void add(ILanguageImplInternal implementation);

    /**
     * Removes a language implementation that no longer belongs to this language.
     * 
     * @param implementation
     *            Language implementation to remove.
     */
    void remove(ILanguageImplInternal implementation);
}
