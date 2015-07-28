package org.metaborg.core.resource;

import java.io.Serializable;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;

public final class ResourceChange implements Serializable {
    private static final long serialVersionUID = -2752072627773810604L;

    /**
     * Resource that has changed.
     */
    public final FileObject resource;

    /**
     * Kind of change.
     */
    public final ResourceChangeKind kind;

    /**
     * Resource based on the value of {@link ResourceChange#kind}.
     * <ul>
     * <li>{@link ResourceChangeKind#Rename}: resource it was renamed from.</li>
     * <li>{@link ResourceChangeKind#Copy}: resource it was copied from.</li>
     * <li>otherwise: null</li>
     * </ul>
     */
    public final @Nullable FileObject from;

    /**
     * Resource based on the value of {@link ResourceChange#kind}.
     * <ul>
     * <li>{@link ResourceChangeKind#Rename}: resource it was renamed to.</li>
     * <li>{@link ResourceChangeKind#Copy}: resource it was copied to.</li>
     * <li>otherwise: null</li>
     * </ul>
     */
    public final @Nullable FileObject to;


    public ResourceChange(FileObject resource, ResourceChangeKind kind, @Nullable FileObject renamedFrom,
        @Nullable FileObject renamedTo) {
        this.resource = resource;
        this.kind = kind;
        this.from = renamedFrom;
        this.to = renamedTo;
    }

    public ResourceChange(FileObject resource, ResourceChangeKind kind) {
        this(resource, kind, null, null);
    }

    public ResourceChange(FileObject resource) {
        this(resource, ResourceChangeKind.Create);
    }


    @Override public String toString() {
        return kind.toString() + " " + resource.toString();
    }
}
