package org.metaborg.spoofax.eclipse.util;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.metaborg.spoofax.core.messages.IMessage;
import org.metaborg.spoofax.core.messages.MessageSeverity;

public final class MarkerUtils {
    public static IMarker createMarker(IResource resource, IMessage message) throws CoreException {
        final String type = IMarker.PROBLEM;
        final IMarker marker = resource.createMarker(type);
        marker.setAttribute(IMarker.CHAR_START, message.region().startOffset());
        marker.setAttribute(IMarker.CHAR_END, message.region().endOffset() + 1);
        marker.setAttribute(IMarker.LINE_NUMBER, message.region().startRow() + 1);
        marker.setAttribute(IMarker.MESSAGE, message.message());
        marker.setAttribute(IMarker.SEVERITY, severity(message.severity()));
        marker.setAttribute(IMarker.PRIORITY, IMarker.PRIORITY_HIGH);
        return marker;
    }

    public static int severity(MessageSeverity severity) {
        switch(severity) {
            case ERROR:
                return IMarker.SEVERITY_ERROR;
            case WARNING:
                return IMarker.SEVERITY_WARNING;
            case NOTE:
                return IMarker.SEVERITY_INFO;
        }
        return IMarker.SEVERITY_INFO;
    }
}
