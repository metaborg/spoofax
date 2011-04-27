package org.strategoxt.imp.runtime.parser.ast;

import static org.eclipse.core.resources.IMarker.PRIORITY_HIGH;
import static org.spoofax.interpreter.core.Tools.termAt;
import static org.spoofax.jsglr.client.imploder.IToken.TK_ERROR;
import static org.spoofax.jsglr.client.imploder.ImploderAttachment.getLeftToken;
import static org.spoofax.jsglr.client.imploder.ImploderAttachment.getRightToken;
import static org.spoofax.jsglr.client.imploder.ImploderAttachment.hasImploderOrigin;
import static org.spoofax.terms.attachments.OriginAttachment.tryGetOrigin;
import static org.strategoxt.imp.runtime.stratego.SourceAttachment.getResource;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.imp.parser.IMessageHandler;
import org.spoofax.interpreter.terms.ISimpleTerm;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.jsglr.client.imploder.IToken;
import org.spoofax.jsglr.client.imploder.ImploderAttachment;
import org.spoofax.jsglr.client.imploder.Token;
import org.strategoxt.imp.runtime.Environment;

/**
 * Reports messages for a group of files, associating
 * errors and other markers with AST nodes.
 * 
 * Threading concerns: should be synchronized externally.
 * Individual methods may document particular threading concerns
 * and exceptions to this rule.
 * 
 * @see IMessageHandler		Handles message reporting for single files.
 * 
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class AstMessageHandler {
	
	public static final String GENERIC_PROBLEM = IMarker.PROBLEM;
	
	public static final String PARSE_MARKER_TYPE = new String("org.strategoxt.imp.runtime.parsemarker"); 

	public static final String ANALYSIS_MARKER_TYPE = new String("org.strategoxt.imp.runtime.analysismarker");
	
	private final String markerType;

	/**
	 * A set of all active markers.
	 * Should be locked using synchronized(asyncActiveMarkers),
	 * but care should be taken not to acquire workspace locks
	 * (as done by setAttributes()) or wait for the main thread
	 * (as done by some other attribute methods) when in a
	 * synchronized lock.
	 */
	private final Set<IMarker> asyncActiveMarkers = new HashSet<IMarker>();
	
	private AstMessageBatch currentBatch;
	
	public AstMessageHandler(String markerType) {
		this.markerType = markerType;
		currentBatch = new AstMessageBatch(markerType, asyncActiveMarkers);
	}
	
	/**
	 * Associates a new marker with a parsed (and possibly rewritten) term.
	 * 
	 * @param severity  The severity of this warning, one of {@link IMarker#SEVERITY_ERROR}, WARNING, or INFO.
	 */
	public void addMarker(IResource resource, IStrategoTerm term, String message,
			int severity) {
		
		ISimpleTerm node = minimizeMarkerSize(getClosestAstNode(term));

		if (node == null) {
			addMarkerFirstLine(resource, message, severity);
			Environment.logWarning("Term is not associated with an AST node, cannot report feedback message: "
							+ term + " - " + message);
		} else if (getResource(node) == null) {
            Environment.logException("Term is not associated with a workspace file, cannot report feedback message: "
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
	public void addMarker(ISimpleTerm node, String message, int severity) {
		if (ImploderAttachment.get(node) == null) {
			Environment.logException("Cannot annotate a tokenless node of type " + node.getClass().getSimpleName() + ": " + node);
			return;
		}
		
		IToken left = getLeftToken(node);
		IToken right = getRightToken(node);

		IResource file = getResource(node);
		
		addMarker(file, left, right, message, severity);
	}

	/**
	 * Associates a new marker with tokens.
	 * 
	 * @param severity  The severity of this warning, one of {@link IMarker#SEVERITY_ERROR}, WARNING, or INFO.
	 */
	public void addMarker(IResource file, IToken left, IToken right, String message, int severity) {
		if (!file.exists()) {
			Environment.logException("Could not create error marker: " + message, new FileNotFoundException(file.toString()));
			return;
		}
		if (left != right && left.getLength() == 1 && left.toString().equals("\n"))
			left = left.getTokenizer().getTokenAt(left.getIndex() + 1);
		MarkerSignature signature = new MarkerSignature(file, left, right, message, severity, PRIORITY_HIGH, false);
		currentBatch.addMarker(signature); // add later
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
				if (firstLine != null) {
					IToken errorToken = new Token(null, 0, 0, 0, 0, firstLine.length(), TK_ERROR);
					addMarker(file, errorToken, errorToken, message, severity);
				} else {
					addMarkerNoLocation(file, message, severity);
				}
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
	private static ISimpleTerm getClosestAstNode(IStrategoTerm term) {
	    if (hasImploderOrigin(term)) {
	        return tryGetOrigin(term);
	    } else if (term == null) {
	    	return null;
	    } else {
	        for (int i = 0; i < term.getSubtermCount(); i++) {
	        	ISimpleTerm result = getClosestAstNode(termAt(term, i));
	            if (result != null) return result;
	        }
	        return null;
	    }
	}
	
	private static ISimpleTerm minimizeMarkerSize(ISimpleTerm node) {
		// TODO: prefer lexical nodes when minimizing marker size? (e.g., not 'private')
		if (node == null) return null;
		while (getLeftToken(node).getLine() < getRightToken(node).getLine()) {
			if (node.getSubtermCount() == 0) break;
			node = node.getSubterm(0);
		}
		return node;
	}

	/**
	 * Add a marker to a file, without having a specific location associated to it.
	 */
	private void addMarkerNoLocation(IResource file, String message, int severity) {
		IToken errorToken = new Token(null, 0, 0, 0, 0, 0, TK_ERROR);
		addMarker(file, errorToken, errorToken, message, severity);
	}
	
	public void clearMarkers(IResource file) {
		if (file.exists())
		  currentBatch.clearMarkers(file);
	}
	
	public final String getMarkerType() {
		return markerType;
	}
	
	/**
	 * Ends (and returns) the current batch of messages and starts a new one.
	 */
	public AstMessageBatch closeBatch() {
		AstMessageBatch lastBatch = currentBatch;
		currentBatch = new AstMessageBatch(markerType, asyncActiveMarkers);
		lastBatch.close();
		return lastBatch;
	}

	/**
	 * Immediately commit all marker changes to Eclipse.
	 */
	public void commitAllChanges() {
		AstMessageBatch batch = closeBatch();
		batch.commitAllChanges();
	}
}
