package org.metaborg.spoofax.eclipse.util;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.metaborg.spoofax.core.messages.IMessage;
import org.metaborg.spoofax.core.messages.MessageSeverity;
import org.metaborg.spoofax.core.messages.MessageType;
import org.metaborg.spoofax.eclipse.SpoofaxPlugin;

public final class MarkerUtils {
    private static final String id = SpoofaxPlugin.id + ".marker";
    private static final String parserId = id + ".parser";
    private static final String analysisId = id + ".analysis";
    private static final String transformationId = id + ".transformation";
    private static final String infoPostfix = ".info";
    private static final String warningPostfix = ".warning";
    private static final String errorPostfix = ".error";


    public static IMarker createMarker(IResource resource, IMessage message) throws CoreException {
        final String type = type(message.type(), message.severity());
        final IMarker marker = resource.createMarker(type);
        marker.setAttribute(IMarker.CHAR_START, message.region().startOffset());
        marker.setAttribute(IMarker.CHAR_END, message.region().endOffset() + 1);
        marker.setAttribute(IMarker.LINE_NUMBER, message.region().startRow() + 1);
        marker.setAttribute(IMarker.MESSAGE, message.message());
        marker.setAttribute(IMarker.SEVERITY, severity(message.severity()));
        marker.setAttribute(IMarker.PRIORITY, IMarker.PRIORITY_NORMAL);
        return marker;
    }

    public static void clearAll(IResource resource) throws CoreException {
        resource.deleteMarkers(id, true, IResource.DEPTH_ONE);
    }

    public static void clearAllRec(IResource resource) throws CoreException {
        resource.deleteMarkers(id, true, IResource.DEPTH_INFINITE);
    }

    public static void clearParser(IResource resource) throws CoreException {
        resource.deleteMarkers(parserId, true, IResource.DEPTH_ONE);
    }

    public static void clearParserRec(IResource resource) throws CoreException {
        resource.deleteMarkers(parserId, true, IResource.DEPTH_INFINITE);
    }

    public static void clearAnalysis(IResource resource) throws CoreException {
        resource.deleteMarkers(analysisId, true, IResource.DEPTH_ONE);
    }

    public static void clearAnalysisRec(IResource resource) throws CoreException {
        resource.deleteMarkers(analysisId, true, IResource.DEPTH_INFINITE);
    }

    public static void clearTransformation(IResource resource) throws CoreException {
        resource.deleteMarkers(transformationId, true, IResource.DEPTH_ONE);
    }

    public static void clearTransformationRec(IResource resource) throws CoreException {
        resource.deleteMarkers(transformationId, true, IResource.DEPTH_INFINITE);
    }

    public static int severity(MessageSeverity severity) {
        switch(severity) {
            case NOTE:
                return IMarker.SEVERITY_INFO;
            case WARNING:
                return IMarker.SEVERITY_WARNING;
            case ERROR:
                return IMarker.SEVERITY_ERROR;
        }
        return IMarker.SEVERITY_INFO;
    }

    public static String type(MessageType type, MessageSeverity severity) {
        final String prefix;
        switch(type) {
            case PARSER_MESSAGE:
                prefix = parserId;
                break;
            case ANALYSIS_MESSAGE:
                prefix = analysisId;
                break;
            case BUILDER_MESSAGE:
                prefix = transformationId;
                break;
            default:
                return id;
        }

        final String postfix;
        switch(severity) {
            case NOTE:
                postfix = infoPostfix;
                break;
            case WARNING:
                postfix = warningPostfix;
                break;
            case ERROR:
                postfix = errorPostfix;
                break;
            default:
                return id;
        }

        return prefix + postfix;
    }
}
