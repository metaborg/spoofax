package org.metaborg.spoofax.core.language.dialect;

import java.util.function.Predicate;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.language.IdentificationFacet;

public class MetaFileIdentifier implements Predicate<FileObject> {
    private final IdentificationFacet identification;

    public MetaFileIdentifier(IdentificationFacet identification) {
        this.identification = identification;
    }


    @Override public boolean test(FileObject resource) {
        if(DialectIdentifier.metaResource(resource) != null) {
            return identification.identify(resource);
        }
        return false;
    }
}
