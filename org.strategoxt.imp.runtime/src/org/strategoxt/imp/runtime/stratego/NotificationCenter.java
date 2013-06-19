package org.strategoxt.imp.runtime.stratego;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * A central, static go-to point for file system notifications.
 * Notifications are sent when files are added, deleted, renamed, or modified.
 * 
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class NotificationCenter {

    private static Map<ObserverDescription, INotificationService> asyncObservers =
        new HashMap<ObserverDescription, INotificationService>();

    /**
     * Notify listeners of an added/removed/moved/modified file with optional partition.
     * 
     * @param file The URI of the file
     * @param partition The partition, or null if not applicable.
     * @param triggerOnSave If the on save handler of changed file should be called.
     */
    public synchronized static void notifyFileChanges(URI file, String partition, boolean triggerOnSave) {
        assert file.isAbsolute();
        for(INotificationService observer : asyncObservers.values()) {
            observer.notifyChanges(file, partition, triggerOnSave);
        }
    }

    /**
     * Notify listeners of multiple added/removed/moved/modified files with optional partitions.
     * 
     * @param files The changed files.
     * @param triggerOnSave If the on save handler of changed file should be called.
     */
    public synchronized static void notifyFileChanges(FilePartition[] files, boolean triggerOnSave) {
        if(files.length == 1) {
            FilePartition file = files[0];
            notifyFileChanges(file.file, file.partition, triggerOnSave);
            return;
        }

        for(FilePartition file : files) {
            assert file.file.isAbsolute();
        }

        for(INotificationService observer : asyncObservers.values()) {
            observer.notifyChanges(files, triggerOnSave);
        }
    }

    /**
     * Notify listener of a new project.
     * 
     * @param project The new project.
     */
    public synchronized static void notifyNewProject(URI project) {
        for(INotificationService observer : asyncObservers.values()) {
            observer.notifyNewProject(project);
        }
    }

    /**
     * Registers an observer. Only one observer is stored at a time for a language/serviceName combination.
     * 
     * @param language The language for this observer, may be null.
     * @param serviceName The name of the service represented by this observer, may be null.
     */
    public synchronized static void putObserver(String language, String serviceName, INotificationService service) {
        asyncObservers.put(new ObserverDescription(language, serviceName), service);
    }

    public synchronized static boolean removeObserver(String language, String service) {
        return asyncObservers.remove(new ObserverDescription(language, service)) != null;
    }

    /**
     * An observer. A wannabe case class.
     * 
     * @author Lennart Kats <lennart add lclnet.nl>
     */
    private static class ObserverDescription {
        final String language;
        final String service;

        public ObserverDescription(String language, String service) {
            this.language = language;
            this.service = service;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((language == null) ? 0 : language.hashCode());
            result = prime * result + ((service == null) ? 0 : service.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if(this == obj)
                return true;
            if(obj == null)
                return false;
            if(!(obj instanceof ObserverDescription))
                return false;
            ObserverDescription other = (ObserverDescription) obj;
            if(language == null) {
                if(other.language != null)
                    return false;
            } else if(!language.equals(other.language))
                return false;
            if(service == null) {
                if(other.service != null)
                    return false;
            } else if(!service.equals(other.service))
                return false;
            return true;
        }

    }
}
