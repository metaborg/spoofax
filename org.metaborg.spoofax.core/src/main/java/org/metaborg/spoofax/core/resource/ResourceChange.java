package org.metaborg.spoofax.core.resource;

import java.io.Serializable;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;

public final class ResourceChange implements IResourceChange, Serializable {
    private static final long serialVersionUID = -2752072627773810604L;

    private final FileObject resource;
    private final ResourceChangeKind kind;
    private final @Nullable FileObject renamedFrom;
    private final @Nullable FileObject renamedTo;


    public ResourceChange(FileObject resource, ResourceChangeKind kind, @Nullable FileObject renamedFrom,
        @Nullable FileObject renamedTo) {
        this.resource = resource;
        this.kind = kind;
        this.renamedFrom = renamedFrom;
        this.renamedTo = renamedTo;
    }

    public ResourceChange(FileObject resource, ResourceChangeKind kind) {
        this(resource, kind, null, null);
    }

    public ResourceChange(FileObject resource) {
        this(resource, ResourceChangeKind.Create);
    }


    @Override public FileObject resource() {
        return resource;
    }

    @Override public ResourceChangeKind kind() {
        return kind;
    }

    @Override public @Nullable FileObject from() {
        return renamedFrom;
    }

    @Override public @Nullable FileObject to() {
        return renamedTo;
    }
}
