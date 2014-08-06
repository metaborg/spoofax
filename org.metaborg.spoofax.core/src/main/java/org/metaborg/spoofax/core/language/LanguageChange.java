package org.metaborg.spoofax.core.language;

public class LanguageChange {
    public enum Kind {
        LOADED, UNLOADED, ACTIVATED, DEACTIVATED
    }


    public final ILanguage language;
    public final Kind kind;


    public LanguageChange(ILanguage language, Kind kind) {
        this.kind = kind;
        this.language = language;
    }
}
