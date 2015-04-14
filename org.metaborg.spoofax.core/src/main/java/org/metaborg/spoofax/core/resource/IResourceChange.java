package org.metaborg.spoofax.core.resource;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;

/**
 * Interface for resource changes.
 */
public interface IResourceChange {
    /**
     * Returns the resource that has changed.
     */
    public abstract FileObject resource();

    /**
     * Returns the kind of change.
     */
    public abstract ResourceChangeKind kind();

    /**
     * When {@link IResourceChange#kind()} returns {@link ResourceChangeKind#Rename}, returns the resource it was
     * renamed from. Otherwise returns null.
     */
    public abstract @Nullable FileObject renamedFrom();

    /**
     * When {@link IResourceChange#kind()} returns {@link ResourceChangeKind#Rename}, returns the resource it was
     * renamed to. Otherwise returns null.
     */
    public abstract @Nullable FileObject renamedTo();
}
