package org.metaborg.core.language;

import jakarta.annotation.Nullable;

/**
 * Represents a change of a language component in the {@link ILanguageService}.
 */
public class LanguageComponentChange {
    public enum Kind {
        Add("addition"), Reload("reload"), Remove("removal");


        private final String name;


        Kind(String name) {
            this.name = name;
        }
        
        @Override public String toString() {
            return name;
        }
    }


    /**
     * Kind of language change.
     */
    public final Kind kind;

    /**
     * Existing language component. Value under different kinds:
     * <ul>
     * <li>{@link Kind#Add}: null</li>
     * <li>{@link Kind#Reload}: Component before the reload.</li>
     * <li>{@link Kind#Remove}: Component that was removed.</li>
     * </ul>
     */
    public final @Nullable ILanguageComponent oldComponent;

    /**
     * New language component. Value under different kinds:
     * <ul>
     * <li>{@link Kind#Add}: Component that was added.</li>
     * <li>{@link Kind#Reload}: Component before the reload.</li>
     * <li>{@link Kind#Remove}: null</li>
     * </ul>
     */
    public final @Nullable ILanguageComponent newComponent;


    public LanguageComponentChange(Kind kind, @Nullable ILanguageComponent oldComponent, @Nullable ILanguageComponent newComponent) {
        this.kind = kind;
        this.oldComponent = oldComponent;
        this.newComponent = newComponent;
    }


    @Override public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + kind.hashCode();
        result = prime * result + ((oldComponent == null) ? 0 : oldComponent.hashCode());
        result = prime * result + ((newComponent == null) ? 0 : newComponent.hashCode());
        return result;
    }

    @Override public boolean equals(Object obj) {
        if(this == obj)
            return true;
        if(obj == null)
            return false;
        if(getClass() != obj.getClass())
            return false;
        final LanguageComponentChange other = (LanguageComponentChange) obj;
        if(kind != other.kind)
            return false;
        if(oldComponent == null) {
            if(other.oldComponent != null)
                return false;
        } else if(!oldComponent.equals(other.oldComponent))
            return false;
        if(newComponent == null) {
            if(other.newComponent != null)
                return false;
        } else if(!newComponent.equals(other.newComponent))
            return false;
        return true;
    }

    @Override public String toString() {
        return String.format("component change [kind=%s, old=%s, new=%s]", kind, oldComponent, newComponent);
    }
}
