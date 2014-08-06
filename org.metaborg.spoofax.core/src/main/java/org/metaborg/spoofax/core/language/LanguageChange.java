package org.metaborg.spoofax.core.language;

/**
 * Represents a change of a language in the {@link ILanguageService}.
 */
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


    @Override public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + kind.hashCode();
        result = prime * result + language.hashCode();
        return result;
    }

    @Override public boolean equals(Object obj) {
        if(this == obj)
            return true;
        if(obj == null)
            return false;
        if(getClass() != obj.getClass())
            return false;
        final LanguageChange other = (LanguageChange) obj;
        if(kind != other.kind)
            return false;
        if(!language.equals(other.language))
            return false;
        return true;
    }

    @Override public String toString() {
        return "LanguageChange [language=" + language + ", kind=" + kind + "]";
    }
}
