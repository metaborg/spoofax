package org.metaborg.spoofax.core.service.identification;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.spoofax.core.language.ILanguageFacet;

import rx.functions.Func1;

/**
 * Represents a facet that can identify resources of this language.
 */
public class IdentificationFacet implements ILanguageFacet {
    private Func1<FileObject, Boolean> identifier;


    /**
     * Creates an identification facet using an identification function.
     * 
     * @param identifier
     *            The identification function.
     */
    public IdentificationFacet(Func1<FileObject, Boolean> identifier) {
        this.identifier = identifier;
    }


    public boolean identify(FileObject file) {
        return identifier.call(file);
    }
}
