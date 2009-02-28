package org.strategoxt.imp.runtime.parser.ast;

import static org.eclipse.core.resources.IMarker.*;
import static org.strategoxt.imp.runtime.parser.tokens.TokenKind.*;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;

import lpg.runtime.IAst;
import lpg.runtime.IToken;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.imp.parser.IMessageHandler;
import org.strategoxt.imp.runtime.Environment;
import org.strategoxt.imp.runtime.parser.tokens.SGLRToken;

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
		
		IToken left = node.getLeftIToken();
		IToken right = node.getRightIToken();

		IResource file = getFile((AstNode) node);
		
		addMarker(file, left, right, message, severity);
	}

	/**
	 * Associates a new marker with tokens.
	 * 
	 * @param severity  The severity of this warning, one of {@link IMarker#SEVERITY_ERROR}, WARNING, or INFO.
	 */
	public void addMarker(IResource file, IToken left, IToken right, String message, int severity) {
		try {
			if (!file.exists()) {
				Environment.logException("Could not create error marker: " + message, new FileNotFoundException(file.toString()));
				return;
			}

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
	
	/**
	 * Add a marker to the first line of a file.
	 */
	public void addMarkerFirstLine(IResource file, String message, int severity) {
		// TODO: Report this error on a proper token or first line of the file?
		if (file instanceof IFile) {
			InputStream stream = null;
			try {
				stream = ((IFile) file).getContents(true);
				BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
				String firstLine = reader.readLine();
				IToken errorToken = new SGLRToken(null, 0, firstLine.length(), TK_ERROR.ordinal());
				addMarker(file, errorToken, errorToken, message, severity);				
			} catch (CoreException e) {
				addMarkerNoLocation(file, message, severity);
			} catch (IOException e) {
				addMarkerNoLocation(file, message, severity);
			} finally {
				try {
					if (stream != null) stream.close();
				} catch (IOException e) {
					Environment.logException(e);
				}
			}
		} else {
			addMarkerNoLocation(file, message, severity);
		}
	}

	/**
	 * Add a marker to a file, without having a specific location associated to it.
	 */
	private void addMarkerNoLocation(IResource file, String message, int severity) {
		IToken errorToken = new SGLRToken(null, 0, 0, TK_ERROR.ordinal());
		addMarker(file, errorToken, errorToken, message, severity);
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
	
	/**
	 * Clear all markers for the specified resource.
	 */
	public void clearMarkers(IResource file) {
		try {
			IMarker[] markers = file.findMarkers(PROBLEM_MARKER_TYPE, true, 0);
			for (IMarker marker : markers) {
				marker.delete();
			}
		} catch (CoreException e) {
			Environment.logException("Unable to clear existing markers for file", e);
		}		
	}

	protected IResource getFile(AstNode node) {
		IPath path = node.getResourcePath();
		IProject project = node.getParseController().getProject().getRawProject();
		path = path.removeFirstSegments(path.matchingFirstSegments(project.getLocation()));
		return project.getFile(path);
	}
}
