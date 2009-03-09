package org.strategoxt.imp.runtime.parser.ast;

import static org.eclipse.core.resources.IMarker.*;
import static org.strategoxt.imp.runtime.dynamicloading.TermReader.*;
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
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.imp.parser.IMessageHandler;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.imp.runtime.Environment;
import org.strategoxt.imp.runtime.parser.tokens.SGLRToken;
import org.strategoxt.imp.runtime.stratego.adapter.IStrategoAstNode;
import org.strategoxt.imp.runtime.stratego.adapter.IWrappedAstNode;

/**
 * Reports messages for a group of files, associating
 * errors and other markers with AST nodes. 
 * 
 * @see IMessageHandler		Handles message reporting for single files.
 * 
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class AstMessageHandler {
	public static final String PARSE_MARKER_TYPE = new String("org.strategoxt.imp.runtime.parsemarker"); 

	public static final String ANALYSIS_MARKER_TYPE = new String("org.strategoxt.imp.runtime.analysismarker");
	
	private final String markerType;

	// TODO: Synchronize access to activeMarkers
	//       (but not to marker API)
	private final Set<IMarker> activeMarkers = new HashSet<IMarker>();
	
	public AstMessageHandler(String markerType) {
		this.markerType = markerType;
	}
	
	/**
	 * Associates a new marker with a parsed (and possibly rewritten) term.
	 * 
	 * @param severity  The severity of this warning, one of {@link IMarker#SEVERITY_ERROR}, WARNING, or INFO.
	 */
	public void addMarker(IResource resource, IStrategoTerm term, String message,
			int severity) {
		
		IAst node = getClosestAstNode(term);

		if (node == null) {
			addMarkerFirstLine(resource, message, severity);
			Environment.logException("ATerm is not associated with an AST node, cannot report feedback message: "
							+ term + " - " + message);
		} else {
			addMarker(node, message, severity);
		}
	}
	
	/**
	 * Associates a new marker with an AST node.
	 * 
	 * @param severity  The severity of this warning, one of {@link IMarker#SEVERITY_ERROR}, WARNING, or INFO.
	 */
	public void addMarker(IAst node, String message, int severity) {
		if (!(node instanceof IStrategoAstNode)) {
			Environment.logException("Cannot annotate a node of type " + node.getClass().getSimpleName() + ": " + node);
			return;
		}
		
		IToken left = node.getLeftIToken();
		IToken right = node.getRightIToken();

		IResource file = ((IStrategoAstNode) node).getSourceInfo().getResource();
		
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

			IMarker marker = file.createMarker(markerType);
			String[] attrs =  { LINE_NUMBER,    CHAR_START,            CHAR_END,                 MESSAGE, SEVERITY, PRIORITY };
			Object[] values = { left.getLine(), left.getStartOffset(), right.getEndOffset() + 1, message, severity, PRIORITY_HIGH };
			marker.setAttributes(attrs, values);
			synchronized (activeMarkers) {
				activeMarkers.add(marker);
			}
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
	 * Given an stratego term, give the first AST node associated
	 * with any of its subterms, doing a depth-first search.
	 */
	private static IAst getClosestAstNode(IStrategoTerm term) {
	    if (term instanceof IWrappedAstNode) {
	        return ((IWrappedAstNode) term).getNode();
	    } else if (term == null) {
	    	return null;
	    } else {
	        for (int i = 0; i < term.getSubtermCount(); i++) {
	        	IAst result = getClosestAstNode(termAt(term, i));
	            if (result != null) return result;
	        }
	        return null;
	    }
	}

	/**
	 * Add a marker to a file, without having a specific location associated to it.
	 */
	private void addMarkerNoLocation(IResource file, String message, int severity) {
		IToken errorToken = new SGLRToken(null, 0, 0, TK_ERROR.ordinal());
		addMarker(file, errorToken, errorToken, message, severity);
	}
	
	/**
	 * Clear all known markers (previously reported by this instance).
	 * For a know resource, use {@link #clearMarkers(IResource)} instead.
	 */
	public void clearAllMarkers() {
		synchronized (activeMarkers) {
			for (IMarker marker : activeMarkers) {
				try {
					marker.delete();
					for (IMarker otherMarker : marker.getResource().findMarkers(markerType, true, 0)) {
						otherMarker.delete();
					}
				} catch (CoreException e) {
					Environment.logException("Unable to delete marker: " + marker, e);
				}
			}
			activeMarkers.clear();
		}
	}
	
	public final void clearMarkers(IStrategoAstNode node) {
		clearMarkers(node.getSourceInfo().getResource());
	}
	
	/**
	 * Clear all markers for the specified resource.
	 */
	public void clearMarkers(IResource file) {
		try {
			IMarker[] markers = file.findMarkers(markerType, true, 0);
			for (IMarker marker : markers) {
				marker.delete();
			}
		} catch (CoreException e) {
			Environment.logException("Unable to clear existing markers for file", e);
		}		
	}
}
