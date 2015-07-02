package org.metaborg.core.context;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.language.ILanguage;
import org.metaborg.core.resource.ResourceService;

public class ContextIdentifier implements Serializable {
    private static final long serialVersionUID = -5397372170660560878L;

    public transient FileObject location;
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


    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        ResourceService.writeFileObject(location, out);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        location = ResourceService.readFileObject(in);
    }
}
