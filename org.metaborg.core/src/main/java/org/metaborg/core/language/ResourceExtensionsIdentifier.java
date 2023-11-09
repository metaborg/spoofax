package org.metaborg.core.language;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.util.collection.Sets;
import org.metaborg.util.iterators.Iterables2;

public class ResourceExtensionsIdentifier implements Predicate<FileObject>, Serializable {
    private static final long serialVersionUID = -7707458553452655759L;
    
	private final Set<String> extensions;


    public ResourceExtensionsIdentifier(Collection<String> extensions) {
        this.extensions = new HashSet<>(extensions);
    }


    @Override public boolean test(FileObject resource) {
        return extensions.contains(resource.getName().getExtension());
    }
}
