package org.metaborg.core.language;

public interface ILanguageComponentInternal extends ILanguageComponent {
    /**
     * @return All language implementations that this component contributes to.
     */
    Iterable<? extends ILanguageImplInternal> contributesToInternal();

    /**
     * Removes all contributing implementations from this component.
     */
    void clearContributions();
}
