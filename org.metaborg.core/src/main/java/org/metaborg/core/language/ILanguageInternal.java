package org.metaborg.core.language;

public interface ILanguageInternal extends ILanguage {
    /**
     * Adds a language implementation that belongs to this language.
     * 
     * @param implementation
     *            Language implementation to add.
     */
    public abstract void add(ILanguageImpl implementation);

    /**
     * Removes a language implementation that no longer belongs to this language.
     * 
     * @param implementation
     *            Language implementation to remove.
     */
    public abstract void remove(ILanguageImpl implementation);
}
