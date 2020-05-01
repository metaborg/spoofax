package org.metaborg.core.language;

import java.util.function.Predicate;

import org.apache.commons.vfs2.FileObject;

/**
 * Represents a facet that can identify resources languages.
 */
public class IdentificationFacet implements IFacet {
    private final Predicate<FileObject> identifier;


    /**
     * Creates an identification facet using an identification function.
     *
     * @param identifier
     *            The identification function.
     */
    public IdentificationFacet(Predicate<FileObject> identifier) {
        this.identifier = identifier;
    }


    public boolean identify(FileObject file) {
        return identifier.test(file);
    }
}
