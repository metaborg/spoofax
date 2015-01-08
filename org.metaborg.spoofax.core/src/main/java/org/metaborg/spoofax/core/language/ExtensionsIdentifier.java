package org.metaborg.spoofax.core.language;

import java.util.Set;

import org.apache.commons.vfs2.FileObject;

import rx.functions.Func1;

import com.google.common.collect.Sets;

public class ExtensionsIdentifier implements Func1<FileObject, Boolean> {
    private Set<String> extensions;


    public ExtensionsIdentifier(Iterable<String> extensions) {
        this.extensions = Sets.newHashSet(extensions);
    }


    @Override public Boolean call(FileObject file) {
        return extensions.contains(file.getName().getExtension());
    }
}
