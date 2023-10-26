package org.metaborg.core.language;

import jakarta.annotation.Nullable;
import java.util.Objects;

/**
 * Represents a change of a language implementation in the {@link ILanguageService}.
 */
public class LanguageImplChange {
    public enum Kind {
        Add, Reload, Remove
    }


    /**
     * Kind of language change.
     */
    public final Kind kind;

    /**
     * Changed language implementation.
     */
    public final @Nullable ILanguageImpl impl;


    public LanguageImplChange(Kind kind, @Nullable ILanguageImpl impl) {
        this.kind = kind;
        this.impl = impl;
    }


    @Override public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + kind.hashCode();
        result = prime * result + (impl != null ? impl.hashCode() : 0);
        return result;
    }

    @Override public boolean equals(Object obj) {
        if(this == obj)
            return true;
        if(obj == null)
            return false;
        if(getClass() != obj.getClass())
            return false;
        final LanguageImplChange other = (LanguageImplChange) obj;
        if(kind != other.kind)
            return false;
        if(!Objects.equals(impl, other.impl))
            return false;
        return true;
    }

    @Override public String toString() {
        return String.format("implementation change [kind=%s, impl=%s]", kind, impl);
    }
}
