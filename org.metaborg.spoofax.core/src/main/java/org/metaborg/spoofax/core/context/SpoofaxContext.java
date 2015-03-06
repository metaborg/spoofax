package org.metaborg.spoofax.core.context;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.spoofax.core.language.ILanguage;

public class SpoofaxContext implements IContext, IContextInternal {
    private final ContextIdentifier identifier;


    public SpoofaxContext(ContextIdentifier identifier) {
        this.identifier = identifier;
    }


    @Override public ContextIdentifier identifier() {
        return identifier;
    }

    @Override public FileObject location() {
        return identifier.location;
    }

    @Override public ILanguage language() {
        return identifier.language;
    }


    @Override public void clean() {

    }

    @Override public void initialize() {

    }

    @Override public void unload() {

    }


    @Override public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + identifier.hashCode();
        return result;
    }

    @Override public boolean equals(Object obj) {
        if(this == obj)
            return true;
        if(obj == null)
            return false;
        if(getClass() != obj.getClass())
            return false;
        final SpoofaxContext other = (SpoofaxContext) obj;
        if(!identifier.equals(other.identifier))
            return false;
        return true;
    }

    @Override public String toString() {
        return String.format("Context for %s, %s", identifier.location, identifier.language);
    }
}
