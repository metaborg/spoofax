package org.metaborg.spoofax.core.language;

import java.util.Set;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.spoofax.core.language.dialect.ResourceDialectIdentifier;

import rx.functions.Func1;

import com.google.common.collect.Sets;

public class ResourceExtensionsIdentifier implements Func1<FileObject, Boolean> {
    private Set<String> extensions;


    public ResourceExtensionsIdentifier(Iterable<String> extensions) {
        this.extensions = Sets.newHashSet(extensions);
    }


    @Override public Boolean call(FileObject resource) {
        if(extensions.contains(resource.getName().getExtension())
            && ResourceDialectIdentifier.metaResource(resource) == null) {
            return true;
        }
        return false;
    }
}
