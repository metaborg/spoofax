package org.strategoxt.imp.runtime.parser.ast;

import static org.eclipse.core.resources.IMarker.PRIORITY_HIGH;
import static org.spoofax.interpreter.core.Tools.termAt;
import static org.spoofax.jsglr.client.imploder.IToken.TK_ERROR;
import static org.spoofax.jsglr.client.imploder.ImploderAttachment.getLeftToken;
import static org.spoofax.jsglr.client.imploder.ImploderAttachment.getRightToken;
import static org.strategoxt.imp.runtime.stratego.SourceAttachment.getResource;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.imp.editor.UniversalEditor;
import org.eclipse.imp.parser.IMessageHandler;
import org.eclipse.imp.parser.IModelListener;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.spoofax.interpreter.terms.ISimpleTerm;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.jsglr.client.imploder.IToken;
import org.spoofax.jsglr.client.imploder.ImploderAttachment;
import org.strategoxt.imp.runtime.Environment;
import org.strategoxt.imp.runtime.parser.tokens.SGLRToken;
import org.strategoxt.imp.runtime.stratego.SourceAttachment;

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
	private volatile Set<IMarker> asyncActiveMarkers = new HashSet<IMarker>();
	
	private final Map<MarkerSignature, IMarker> markersToReuse = new HashMap<MarkerSignature, IMarker>();
	
	private final List<IMarker> markersToDelete = new ArrayList<IMarker>();
	
	private final List<MarkerSignature> markersToAdd = new ArrayList<MarkerSignature>();
	
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
		
		ISimpleTerm node = minimizeMarkerSize(getClosestAstNode(term));

		if (node == null) {
			addMarkerFirstLine(resource, message, severity);
			Environment.logException("Term is not associated with an AST node, cannot report feedback message: "
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
		assert !Thread.holdsLock(asyncActiveMarkers) : "Potential deadlock: need main thread access for markers";

		try {
			if (!file.exists()) {
				Environment.logException("Could not create error marker: " + message, new FileNotFoundException(file.toString()));
				return;
			}
			
			MarkerSignature signature = new MarkerSignature(file, left, right, message, severity, PRIORITY_HIGH, false);
			
			IMarker marker = markersToReuse.remove(signature);
			if (marker == null || !marker.exists()) {
				markersToAdd.add(signature); // add later
			} else {
				if (!signature.attibutesEqual(marker))
					marker.setAttributes(signature.getAttributes(), signature.getValues());
				
				synchronized (asyncActiveMarkers) {
					asyncActiveMarkers.add(marker);
				}
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
				if (firstLine != null) {
					IToken errorToken = new SGLRToken(null, 0, firstLine.length(), TK_ERROR);
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
	    if (term instanceof OneOfThoseTermsWithOriginInformation) {
	        return ((IStrategoTerm) term).getNode();
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
		while (getLeftToken(node).getLine() < getRightToken(node).getEndLine()) {
			if (node.getSubtermCount() == 0) break;
			node = node.getSubterm(0);
		}
		return node;
	}

	/**
	 * Add a marker to a file, without having a specific location associated to it.
	 */
	private void addMarkerNoLocation(IResource file, String message, int severity) {
		IToken errorToken = new SGLRToken(null, 0, 0, TK_ERROR);
		addMarker(file, errorToken, errorToken, message, severity);
	}
	
	/**
	 * Clear all known markers (previously reported by this instance).
	 * For a know resource, use {@link #clearMarkers(IResource)} instead.
	 * 
	 * @see #commitAllChanges()  To commit the changes made by this action.
	 */
	public void clearAllMarkers() {
		// Copy the active markers so we only need to synchronize here,
		// and not in the loop (risking deadlocks)
		Set<IMarker> toDelete;
		synchronized (asyncActiveMarkers) {
			toDelete = asyncActiveMarkers;
			asyncActiveMarkers = null;
		}
		
		for (IMarker marker : toDelete) {
			try {
				markersToReuse.put(new MarkerSignature(marker), marker);
				for (IMarker otherMarker : marker.getResource().findMarkers(markerType, true, 0)) {
					IMarker dupe = markersToReuse.put(new MarkerSignature(otherMarker), otherMarker);
					if (dupe != null) markersToDelete.add(dupe);
				}
			} catch (CoreException e) {
				Environment.logException("Unable find related markers: " + marker, e);
			}
		}
		markersToAdd.clear();
	}
	
	/**
	 * Clear all markers for the specified resource.
	 * 
	 * @see #commitDeletions()  To commit the changes made by this action.
	 */
	public void clearMarkers(IResource file) {
		try {
			for (IMarker marker : file.findMarkers(markerType, true, 0)) {
				IMarker dupe = markersToReuse.put(new MarkerSignature(marker), marker);
				if (dupe != null) markersToDelete.add(dupe);
			}
			for (IMarker marker : file.findMarkers(GENERIC_PROBLEM, false, 0)) {
				// Remove legacy markers (Spoofax/195)
				markersToDelete.add(marker);
			}
			Iterator<MarkerSignature> markersToAdd = this.markersToAdd.iterator();
			while (markersToAdd.hasNext()) {
				MarkerSignature marker = markersToAdd.next();
				if (marker.getResource().equals(file)) markersToAdd.remove(); 
			}
		} catch (CoreException e) {
			Environment.logException("Unable to clear existing markers for file: " + file.getName(), e);
		}		
	}
	
	/**
	 * Adds any markers not previously present and
	 * deletes markers as instructed using clearMarkers().
	 */
	public void commitAllChanges() {
		runInWorkspace(new Runnable() {
			public void run() {
				commitAdditions();
				commitDeletions();
			}
		});
	}

	/**
	 * Deletes markers as instructed using clearMarkers().
	 */
	public void commitDeletions() {
		try {
			runInWorkspace(new Runnable() {
				public void run() {
					// Delete reusable markers that were not reused
					deleteMarkers(markersToReuse.values());
					markersToReuse.clear();
					// Delete non-reusable non-reused markers
					deleteMarkers(markersToDelete);
					markersToDelete.clear();
				}
			});
		} catch (RuntimeException e) {
			Environment.logException("Unable to clear markers", e);
		}
	}
	
	/**
	 * Commit any newly added error markers that are on lines with 
	 * that previously had or currently have other error markers
	 * (these should typically not be displayed using a delay).
	 * 
	 * If this method is not called, multi-error line markers will still be
	 * added by {@link #commitAdditions()}.
	 */
	public void commitMultiErrorLineAdditions() {
		runInWorkspace(new Runnable() {
			public void run() {
				commitMultiErrorLineAdditionsInWS();
			}
		});
	}
	
	private void commitMultiErrorLineAdditionsInWS() {	
		assert !Thread.holdsLock(asyncActiveMarkers) : "Potential deadlock: need main thread access for markers";

		// Clone active markers so we only need to synchronize here
		// and not in markerExistsOnSameLine
		Set<IMarker> activeMarkers;
		synchronized (asyncActiveMarkers) {
			activeMarkers = new HashSet<IMarker>(asyncActiveMarkers);
		}
		
		Iterator<MarkerSignature> signatures = markersToAdd.iterator();
		while (signatures.hasNext()) {
			MarkerSignature signature = signatures.next();
			if (markerExistsOnSameLine(signature, activeMarkers)) {
				try {
					IMarker marker = signature.getResource().createMarker(markerType);
					marker.setAttributes(signature.getAttributes(), signature.getValues());
					synchronized (asyncActiveMarkers) {
						asyncActiveMarkers.add(marker);
					}
				} catch (CoreException e) {
					Environment.logException("Could not create error marker: " + signature.getMessage(), e);
				}
				signatures.remove();
			}
		}
	}
	
	private boolean markerExistsOnSameLine(MarkerSignature signature, Set<IMarker> activeMarkers) {
		// TODO: optimize markerExistsOnSameLine()?
		IResource resource = signature.getResource();
		int line = signature.getLine();
		for (IMarker marker : activeMarkers) {
			if (marker.getResource().equals(resource)
					&& marker.getAttribute(IMarker.LINE_NUMBER, -1) == line) {
				return true;
			}
		}
		for (IMarker marker : markersToReuse.values()) {
			if (marker.getResource().equals(resource)
					&& marker.getAttribute(IMarker.LINE_NUMBER, -1) == line) {
				return true;
			}
		}
		for (IMarker marker : markersToDelete) {
			if (marker.getResource().equals(resource)
					&& marker.getAttribute(IMarker.LINE_NUMBER, -1) == line) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Commit any newly added error markers.
	 * 
	 * @see #asyncCommitAdditions(List)  A thread-safe variation of this method.
	 */
	public void commitAdditions() {
		commitAdditions(this.markersToAdd);
	}

	/**
	 * Commit any newly added error markers.
	 * Can be invoked asynchronously, but care should be taken
	 * that only one thread can access the 'markers' input list. 
	 */
	public List<IMarker> asyncCommitAdditions(List<MarkerSignature> markers) {
		Environment.assertNotMainThread();
		assert Thread.holdsLock(getSyncRoot());

		return commitAdditions(markers);
	}

	private List<IMarker> commitAdditions(final List<MarkerSignature> markers) {
		class Action implements Runnable {
			List<IMarker> results;
			
			public void run() {
				results = commitAdditionsInWS(markers);
			}
		};
		
		Action action = new Action();
		runInWorkspace(action);
		return action.results;
	}

	private List<IMarker> commitAdditionsInWS(List<MarkerSignature> markers) {
		assert !Environment.isMainThread() || !Thread.holdsLock(asyncActiveMarkers) : "Potential deadlock"; 

		List<IMarker> results = new ArrayList<IMarker>();
		for (MarkerSignature signature : markers) {
			try {
				IMarker marker = signature.getResource().createMarker(markerType);
				marker.setAttributes(signature.getAttributes(), signature.getValues());
				synchronized (asyncActiveMarkers) {
					asyncActiveMarkers.add(marker);
				}
				results.add(marker);
			} catch (CoreException e) {
				Environment.logException("Could not create error marker: " + signature.getMessage(), e);
			}
		}
		return results;
	}
	
	public void asyncDeleteMarkers(Collection<IMarker> markers) {
		Environment.assertNotMainThread();
		assert Thread.holdsLock(getSyncRoot());

		deleteMarkers(markers);
	}

	private void deleteMarkers(Collection<IMarker> markers) {
		assert !Environment.isMainThread() || !Thread.holdsLock(asyncActiveMarkers) : "Potential deadlock"; 
		
		for (IMarker marker : markers) {
			try {
				synchronized (asyncActiveMarkers) {
					asyncActiveMarkers.remove(marker);
				}
				marker.delete();
			} catch (CoreException e) {
				Environment.logException("Unable to clear existing marker", e);
			}
		}
	}
	
	public Object getSyncRoot() {
		return asyncActiveMarkers;
	}
	
	public final String getMarkerType() {
		return markerType;
	}
	
	/**
	 * Returns a copy of the list of markers scheduled to be added.
	 */
	public List<MarkerSignature> getAdditions() {
		return new ArrayList<MarkerSignature>(markersToAdd);
	}
	
	/*
	 * Returns a copy of the list of markers scheduled to be deleted.
	 * 
	 * @param includeReuseCandidates  Whether or not to include markers potentially reusable for addition.
	 *
	public List<IMarker> getDeletions(boolean includeReuseCandidates) {
		ArrayList<IMarker> results = new ArrayList<IMarker>(markersToDelete);
		if (includeReuseCandidates)
			results.addAll(markersToReuse.values());
		return results;
	}
	 */
	
	private static void runInWorkspace(final Runnable action) {
		try {
			ResourcesPlugin.getWorkspace().run(new IWorkspaceRunnable() {
					public void run(IProgressMonitor monitor) {
						action.run();
					}
				}, null, IWorkspace.AVOID_UPDATE, new NullProgressMonitor()
				);
		} catch (CoreException e) {
			Environment.logException("Exception in message handler", e);
		}
	}

	/**
	 * Force editor recoloring events to be processed,
	 * ensuring proper syntax highlighting after markers have been added
	 * or deleted.
	 * 
	 * Swallows and logs all exceptions.
	 */
	@Deprecated
	public static void processAllEditorRecolorEvents() {
		try {
			for (IWorkbenchWindow window : PlatformUI.getWorkbench().getWorkbenchWindows()) {
				IWorkbenchPage page = window.getActivePage();
				for (IEditorReference ref : page.getEditorReferences()) {
					IEditorPart editor = ref.getEditor(false);
					if (editor instanceof UniversalEditor) {
						UniversalEditor universalEditor = (UniversalEditor) editor;
						processEditorRecolorEvents(universalEditor);
					}
				}
			}
		} catch (RuntimeException e) {
			Environment.logException("Could not update editor coloring", e);
		}
		
	}

	/**
	 * Force editor recoloring events to be processed,
	 * ensuring proper syntax highlighting after markers have been added
	 * or deleted.
	 * 
	 * Swallows and logs all exceptions.
	 */
	@Deprecated
	public static void processEditorRecolorEvents(UniversalEditor universalEditor) {
		assert !Environment.getStrategoLock().isHeldByCurrentThread() : "Potential deadlock";
		try {
			IModelListener presentation = universalEditor.getServiceControllerManager().getPresentationController();
			presentation.update(universalEditor.getParseController(), new NullProgressMonitor());
		} catch (RuntimeException e) {
			Environment.logException("Could not update editor coloring", e);
		}
	}
}
