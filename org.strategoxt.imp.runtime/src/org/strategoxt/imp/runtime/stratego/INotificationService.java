package org.strategoxt.imp.runtime.stratego;

import java.net.URI;

/**
 * @author Lennart Kats <lennart add lclnet.nl>
 * @author Gabriël Konat
 */
public interface INotificationService {
    /**
     * Notify listener of a added/removed/changed file with optional partition.
     * 
     * @param file The URI of the file
     * @param partition The partition, or null if not applicable.
     * @param triggerOnSave If the on save handler of changed file should be called.
     */
    void notifyChanges(URI file, String partition, boolean triggerOnSave);

    /**
     * Notify listeners of multiple added/removed/changed files with optional partitions.
     * 
     * @param files The changed files.
     * @param triggerOnSave If the on save handler of changed file should be called.
     */
    void notifyChanges(FilePartition[] files, boolean triggerOnSave);

    /**
     * Notify listener of a new project.
     * 
     * @param project The new project.
     */
    void notifyNewProject(URI project);
}
