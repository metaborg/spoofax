package org.metaborg.core.language;

import java.io.Serializable;
import java.util.Set;
import java.util.function.Predicate;

import org.apache.commons.vfs2.FileObject;

import com.google.common.collect.Sets;

public class ResourceExtensionsIdentifier implements Predicate<FileObject>, Serializable {
    private static final long serialVersionUID = -7707458553452655759L;
    
	private final Set<String> extensions;


    public ResourceExtensionsIdentifier(Iterable<String> extensions) {
        this.extensions = Sets.newHashSet(extensions);
    }


    @Override public boolean test(FileObject resource) {
        return extensions.contains(resource.getName().getExtension());
    }
}
