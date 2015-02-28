package org.metaborg.spoofax.eclipse.util;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * Utility functions for creating {@link IStatus} instances.
 */
public final class StatusUtils {
    public static IStatus success() {
        return new Status(IStatus.OK, "org.metaborg.spoofax.eclipse", IStatus.OK, "", null);
    }

    public static IStatus success(String message) {
        return new Status(IStatus.OK, "org.metaborg.spoofax.eclipse", IStatus.OK, message, null);
    }

    public static IStatus cancel() {
        return new Status(IStatus.CANCEL, "org.metaborg.spoofax.eclipse", IStatus.CANCEL, "", null);
    }

    public static IStatus cancel(String message) {
        return new Status(IStatus.CANCEL, "org.metaborg.spoofax.eclipse", IStatus.CANCEL, message, null);
    }


    public static IStatus info(String message) {
        return new Status(IStatus.INFO, "org.metaborg.spoofax.eclipse", IStatus.ERROR, message, null);
    }


    public static IStatus warn(String message) {
        return new Status(IStatus.WARNING, "org.metaborg.spoofax.eclipse", IStatus.ERROR, message, null);
    }

    public static IStatus warn(Throwable t) {
        return new Status(IStatus.WARNING, "org.metaborg.spoofax.eclipse", IStatus.ERROR, "", t);
    }

    public static IStatus warn(String message, Throwable t) {
        return new Status(IStatus.WARNING, "org.metaborg.spoofax.eclipse", IStatus.ERROR, message, t);
    }


    public static IStatus error() {
        return new Status(IStatus.ERROR, "org.metaborg.spoofax.eclipse", IStatus.ERROR, "", null);
    }

    public static IStatus error(String message) {
        return new Status(IStatus.ERROR, "org.metaborg.spoofax.eclipse", IStatus.ERROR, message, null);
    }

    public static IStatus error(Throwable t) {
        return new Status(IStatus.ERROR, "org.metaborg.spoofax.eclipse", IStatus.ERROR, "", t);
    }

    public static IStatus error(String message, Throwable t) {
        return new Status(IStatus.ERROR, "org.metaborg.spoofax.eclipse", IStatus.ERROR, message, t);
    }
}
