package org.metaborg.spoofax.core.context;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.spoofax.core.language.ILanguage;

public class ContextIdentifier {
    public final FileObject location;
    public final ILanguage language;


    public ContextIdentifier(FileObject location, ILanguage language) {
        this.location = location;
        this.language = language;
    }


    @Override public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((location == null) ? 0 : location.hashCode());
        result = prime * result + ((language == null) ? 0 : language.hashCode());
        return result;
    }

    @Override public boolean equals(Object obj) {
        if(this == obj)
            return true;
        if(obj == null)
            return false;
        if(getClass() != obj.getClass())
            return false;
        final ContextIdentifier other = (ContextIdentifier) obj;
        if(!language.equals(other.language))
            return false;
        if(!location.equals(other.location))
            return false;
        return true;
    }
}
