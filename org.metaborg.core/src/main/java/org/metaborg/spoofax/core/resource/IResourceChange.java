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
     * Returns a resource based on the value of {@link IResourceChange#kind()}.
     * <ul>
     * <li>{@link ResourceChangeKind#Rename}: resource it was renamed from.</li>
     * <li>{@link ResourceChangeKind#Copy}: resource it was copied from.</li>
     * <li>otherwise: null</li>
     * </ul>
     */
    public abstract @Nullable FileObject from();

    /**
     * Returns a resource based on the value of {@link IResourceChange#kind()}.
     * <ul>
     * <li>{@link ResourceChangeKind#Rename}: resource it was renamed to.</li>
     * <li>{@link ResourceChangeKind#Copy}: resource it was copied to.</li>
     * <li>otherwise: null</li>
     * </ul>
     */
    public abstract @Nullable FileObject to();
}
