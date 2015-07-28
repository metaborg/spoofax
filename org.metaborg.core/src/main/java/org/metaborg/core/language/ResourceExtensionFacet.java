package org.metaborg.core.language;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import com.google.common.collect.Lists;

/**
 * Represents a facet that specifies the file extensions for languages. Complements the {@link IdentificationFacet}.
 */
public class ResourceExtensionFacet implements ILanguageFacet {
    private static final long serialVersionUID = -1843776488565651809L;

    private transient Iterable<String> extensions;


    public ResourceExtensionFacet(Iterable<String> extensions) {
        this.extensions = extensions;
    }


    public Iterable<String> extensions() {
        return extensions;
    }


    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        if(extensions instanceof Serializable) {
            out.writeObject(extensions);
        } else {
            out.writeObject(Lists.newArrayList(extensions));
        }
    }

    @SuppressWarnings("unchecked") private void readObject(ObjectInputStream in) throws ClassNotFoundException,
        IOException {
        in.defaultReadObject();
        extensions = (Iterable<String>) in.readObject();
    }
}
