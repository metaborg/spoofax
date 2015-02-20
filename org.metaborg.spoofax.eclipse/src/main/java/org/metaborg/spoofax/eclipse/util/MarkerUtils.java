package org.metaborg.spoofax.eclipse.util;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.metaborg.spoofax.core.messages.IMessage;
import org.metaborg.spoofax.core.messages.MessageSeverity;
import org.metaborg.spoofax.core.messages.MessageType;
import org.metaborg.spoofax.eclipse.SpoofaxPlugin;

/**
 * Utility functions for creating and removing {@link IMarker} instances.
 */
public final class MarkerUtils {
    private static final String id = SpoofaxPlugin.id + ".marker";
    private static final String parserId = id + ".parser";
    private static final String analysisId = id + ".analysis";
    private static final String transformationId = id + ".transformation";
    private static final String infoPostfix = ".info";
    private static final String warningPostfix = ".warning";
    private static final String errorPostfix = ".error";


    /**
     * Creates a marker for given resource, from given message.
     * 
     * @param resource
     *            Resource to create a marker on.
     * @param message
     *            Message to create the marker with.
     * @return Created marker.
     * @throws CoreException
     *             When creating the marker fails.
     */
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


    /**
     * Clears all Spoofax markers from given resource.
     * 
     * @param resource
     *            Resource to clear markers for.
     * @throws CoreException
     *             When clearing markers fails.
     */
    public static void clearAll(IResource resource) throws CoreException {
        resource.deleteMarkers(id, true, IResource.DEPTH_ZERO);
    }

    /**
     * Clears all Spoofax markers from given resource, and all Spoofax markers from its transitive child resources.
     * 
     * @param resource
     *            Resource to clear markers for.
     * @throws CoreException
     *             When clearing markers fails.
     */
    public static void clearAllRec(IResource resource) throws CoreException {
        resource.deleteMarkers(id, true, IResource.DEPTH_INFINITE);
    }

    /**
     * Clears all parse markers from given resource.
     * 
     * @param resource
     *            Resource to clear markers for.
     * @throws CoreException
     *             When clearing markers fails.
     */
    public static void clearParser(IResource resource) throws CoreException {
        resource.deleteMarkers(parserId, true, IResource.DEPTH_ZERO);
    }

    /**
     * Clears all parse markers from given resource, and all parse markers from its transitive child resources.
     * 
     * @param resource
     *            Resource to clear markers for.
     * @throws CoreException
     *             When clearing markers fails.
     */
    public static void clearParserRec(IResource resource) throws CoreException {
        resource.deleteMarkers(parserId, true, IResource.DEPTH_INFINITE);
    }

    /**
     * Clears all analysis markers from given resource.
     * 
     * @param resource
     *            Resource to clear markers for.
     * @throws CoreException
     *             When clearing markers fails.
     */
    public static void clearAnalysis(IResource resource) throws CoreException {
        resource.deleteMarkers(analysisId, true, IResource.DEPTH_ZERO);
    }

    /**
     * Clears all analysis markers from given resource, and all analysis markers from its transitive child resources.
     * 
     * @param resource
     *            Resource to clear markers for.
     * @throws CoreException
     *             When clearing markers fails.
     */
    public static void clearAnalysisRec(IResource resource) throws CoreException {
        resource.deleteMarkers(analysisId, true, IResource.DEPTH_INFINITE);
    }

    /**
     * Clears all transformation markers from given resource.
     * 
     * @param resource
     *            Resource to clear markers for.
     * @throws CoreException
     *             When clearing markers fails.
     */
    public static void clearTransformation(IResource resource) throws CoreException {
        resource.deleteMarkers(transformationId, true, IResource.DEPTH_ZERO);
    }

    /**
     * Clears all transformation markers from given resource, and all transformation markers from its transitive child
     * resources.
     * 
     * @param resource
     *            Resource to clear markers for.
     * @throws CoreException
     *             When clearing markers fails.
     */
    public static void clearTransformationRec(IResource resource) throws CoreException {
        resource.deleteMarkers(transformationId, true, IResource.DEPTH_INFINITE);
    }


    /**
     * Converts a Spoofax severity into an Eclipse severity.
     * 
     * @param severity
     *            Spoofax severity.
     * @return Eclipse severity.
     */
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

    /**
     * Converts a Spoofax message type and severity, into an Eclipse message type.
     * 
     * @param type
     *            Spoofax message type.
     * @param severity
     *            Spoofax severity.
     * @return Eclipse message type.
     */
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
