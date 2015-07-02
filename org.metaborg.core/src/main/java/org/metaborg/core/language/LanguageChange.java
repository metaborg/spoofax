package org.metaborg.core.language;

import javax.annotation.Nullable;

/**
 * Represents a change of a language in the {@link ILanguageService}.
 */
public class LanguageChange {
    public enum Kind {
        /**
         * Language is loaded. Only fired if a language with the same name did not exist before. Always fired before
         * {@link #ADD}.
         */
        ADD_FIRST,
        /**
         * Language is added. Also fired if language with the same name existed before.
         */
        ADD,
        /**
         * Active language is replaced by another language with a different version or location.
         */
        REPLACE_ACTIVE,
        /**
         * Active language is reloaded. Fired instead of {@link #RELOAD}.
         */
        RELOAD_ACTIVE,
        /**
         * Inactive language is reloaded.
         */
        RELOAD,
        /**
         * Language is removed. Also fired if language with the same name will still exist. Always fired before
         * {@link #UNLOAD}.
         */
        REMOVE,
        /**
         * Language is unloaded. Only fired if language with this name will not exist any more.
         */
        REMOVE_LAST
    }


    /**
     * Kind of language change.
     */
    public final Kind kind;

    /**
     * Existing language. Value under different kinds:
     * <ul>
     * <li>{@link Kind#ADD_FIRST}: null</li>
     * <li>{@link Kind#ADD}: null</li>
     * <li>{@link Kind#REPLACE_ACTIVE}: Language that is being replaced.</li>
     * <li>{@link Kind#RELOAD_ACTIVE}: Language before the reload. Equal to {@link #newLanguage}, but creation date and
     * facets may differ.</li>
     * <li>{@link Kind#RELOAD}: Language before the reload. Equal to {@link #newLanguage}, but creation date and facets
     * may differ.</li>
     * <li>{@link Kind#REMOVE}: Language that was removed.</li>
     * <li>{@link Kind#REMOVE_LAST}: Language that was unloaded.</li>
     * </ul>
     */
    public final @Nullable ILanguage oldLanguage;

    /**
     * New language. Value under different kinds:
     * <ul>
     * <li>{@link Kind#ADD_FIRST}: Language that was loaded.</li>
     * <li>{@link Kind#ADD}: Language that was added.</li>
     * <li>{@link Kind#REPLACE_ACTIVE}: Language that is replacing previously active language.</li>
     * <li>{@link Kind#RELOAD_ACTIVE}: Language before the reload. Equal to {@link #newLanguage}, but creation date and
     * facets may differ.</li>
     * <li>{@link Kind#RELOAD}: Language before the reload. Equal to {@link #newLanguage}, but creation date and facets
     * may differ.</li>
     * <li>{@link Kind#REMOVE}: null</li>
     * <li>{@link Kind#REMOVE_LAST}: null</li>
     * </ul>
     */
    public final @Nullable ILanguage newLanguage;


    public LanguageChange(Kind kind, ILanguage oldLanguage, ILanguage newLanguage) {
        this.kind = kind;
        this.oldLanguage = oldLanguage;
        this.newLanguage = newLanguage;
    }


    @Override public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + kind.hashCode();
        result = prime * result + ((oldLanguage == null) ? 0 : oldLanguage.hashCode());
        result = prime * result + ((newLanguage == null) ? 0 : newLanguage.hashCode());
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
        if(oldLanguage == null) {
            if(other.oldLanguage != null)
                return false;
        } else if(!oldLanguage.equals(other.oldLanguage))
            return false;
        if(newLanguage == null) {
            if(other.newLanguage != null)
                return false;
        } else if(!newLanguage.equals(other.newLanguage))
            return false;
        return true;
    }

    @Override public String toString() {
        return String
            .format("LanguageChange [kind=%s, oldLanguage=%s, newLanguage=%s]", kind, oldLanguage, newLanguage);
    }
}
