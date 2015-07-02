package org.metaborg.spoofax.core.language.dialect;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.language.IdentificationFacet;

import rx.functions.Func1;

public class MetaFileIdentifier implements Func1<FileObject, Boolean> {
    private final IdentificationFacet identification;

    public MetaFileIdentifier(IdentificationFacet identification) {
        this.identification = identification;
    }


    @Override public Boolean call(FileObject resource) {
        if(StrategoDialectIdentifier.metaResource(resource) != null) {
            return identification.identify(resource);
        }
        return false;
    }
}
