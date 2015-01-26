package org.metaborg.spoofax.eclipse.util;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

public class SpoofaxStatus {
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
        return new Status(IStatus.CANCEL, "org.metaborg.spoofax.eclipse", IStatus.CANCEL, message,
            null);
    }

    public static IStatus error() {
        return new Status(IStatus.ERROR, "org.metaborg.spoofax.eclipse", IStatus.ERROR, "", null);
    }

    public static IStatus error(String message) {
        return new Status(IStatus.ERROR, "org.metaborg.spoofax.eclipse", IStatus.ERROR, message,
            null);
    }

    public static IStatus error(Exception e) {
        return new Status(IStatus.ERROR, "org.metaborg.spoofax.eclipse", IStatus.ERROR, "", e);
    }

    public static IStatus error(String message, Exception e) {
        return new Status(IStatus.ERROR, "org.metaborg.spoofax.eclipse", IStatus.ERROR, message, e);
    }
}
