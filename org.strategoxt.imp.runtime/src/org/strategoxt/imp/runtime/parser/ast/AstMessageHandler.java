package org.strategoxt.imp.runtime.parser.ast;

import static org.eclipse.core.resources.IMarker.*;

import java.util.HashSet;
import java.util.Set;

import lpg.runtime.IAst;
import lpg.runtime.IToken;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.imp.parser.IMessageHandler;
import org.strategoxt.imp.runtime.Environment;

/**
 * Reports messages for a group of files, associating
 * errors and other markers with AST nodes. 
 * 
 * @see IMessageHandler		Handles message reporting for single files.
 * 
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class AstMessageHandler {
	private static final String PROBLEM_MARKER_TYPE = "org.eclipse.core.resources.problemmarker";

	private final Set<IMarker> activeMarkers = new HashSet<IMarker>();
	
	/**
	 * Associates a new marker with an AST node.
	 * 
	 * @param severity  The severity of this warning, one of {@link IMarker#SEVERITY_ERROR}, WARNING, or INFO.
	 */
	public void addMarker(IAst node, String message, int severity) {
		if (!(node instanceof AstNode)) { // TODO: Be less SGLR specific?
			Environment.logException("Cannot annotate a node of type " + node.getClass().getSimpleName() + ": " + node);
			return;
		}
		
		IFile file = getFile((AstNode) node);
		if (!file.exists()) return;
		
		IToken left = node.getLeftIToken();
		IToken right = node.getRightIToken();
		
		try {
			IMarker marker = file.createMarker(PROBLEM_MARKER_TYPE);
			marker.setAttribute(LINE_NUMBER, left.getLine());
			marker.setAttribute(CHAR_START, left.getStartOffset());
			marker.setAttribute(CHAR_END, right.getEndOffset() + 1);
			marker.setAttribute(MESSAGE, message);
			marker.setAttribute(SEVERITY, severity);
			marker.setAttribute(PRIORITY, PRIORITY_HIGH);
			activeMarkers.add(marker);
		} catch (CoreException e) {
			Environment.logException("Could not create error marker: " + message, e);
		}
	}
	
	public void clearAllMarkers() {
		for (IMarker marker : activeMarkers) {
			try {
				marker.delete();
				for (IMarker otherMarker : marker.getResource().findMarkers(PROBLEM_MARKER_TYPE, true, 0)) {
					otherMarker.delete();
				}
			} catch (CoreException e) {
				Environment.logException("Unable to delete marker: " + marker, e);
			}
		}
		activeMarkers.clear();
	}
	
	public final void clearMarkers(AstNode node) {
		clearMarkers(getFile(node));
	}
	
	public void clearMarkers(IFile file) {
		try {
			IMarker[] markers = file.findMarkers(PROBLEM_MARKER_TYPE, true, 0);
			for (IMarker marker : markers) {
				marker.delete();
			}
		} catch (CoreException e) {
			Environment.logException("Unable to clear existing markers for file", e);
		}		
	}

	protected IFile getFile(AstNode node) {
		IPath path = node.getResourcePath();
		IProject project = node.getParseController().getProject().getRawProject();
		path = path.removeFirstSegments(path.matchingFirstSegments(project.getLocation()));
		return project.getFile(path);
	}
}
