package org.strategoxt.imp.runtime.parser.ast;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.strategoxt.imp.runtime.Environment;

/**
 * One batch of messages. The point of this class is to be able to build a
 * batch of markers (e.g. during parsing) without any threading concerns,
 * and then pass the whole batch to a UIJob for display in the editor.<br/><br/> 
 * 
 * This separation is intended to prevent deadlocks involving Eclipse locks
 * acquired by the marker API and e.g. the Spoofax parseLock.<br/><br/>
 * 
 * Typically the batch should be filled using {@link #addMarker(MarkerSignature)},
 * {@link #deleteMarker(IMarker)} and {@link #clearMarkers(IResource)}.
 * Then the batch should be committed, possibly in an UIJob, using some of the
 * commit methods.<br/><br/>
 * 
 * This class may not be used by multiple threads at the same time, but it may
 * be passed to a different thread right after a call to {@link #close()}.  
 * 
 * @author Tobi Vollebregt (factored this out of {@link AstMessageHandler})
 */
public class AstMessageBatch {
	
	private final String markerType;
	
	private final Set<IMarker> asyncActiveMarkers;
	
	private final Map<MarkerSignature, IMarker> markersToReuse = new HashMap<MarkerSignature, IMarker>();
	
	private final List<IMarker> markersToDelete = new ArrayList<IMarker>();
	
	private final List<MarkerSignature> markersToAdd = new ArrayList<MarkerSignature>();
	
	private boolean closed = false;
	
	/**
	 * Create a new batch of messages (markers).
	 * 
	 * The set asyncActiveMarkers is updated immediately when markers are
	 * added/deleted using the Eclipse API; i.e. it should always represent
	 * the current set of markers known to Eclipse.
	 * 
	 * Each change to asyncActiveMarkers happens in a
	 * synchronized(asyncActiveMarkers) block.
	 */
	public AstMessageBatch(String markerType, Set<IMarker> asyncActiveMarkers) {
		this.markerType = markerType;
		this.asyncActiveMarkers = asyncActiveMarkers;
	}
	
	/**
	 * @return True iff this batch does not represents any marker changes.
	 */
	public boolean isEmpty() {
		return markersToReuse.isEmpty() && markersToDelete.isEmpty() && markersToAdd.isEmpty();
	}
	
	/**
	 * Schedule a marker for deletion. It may be reused for a newly added
	 * marker in {@link #commitReuseableAdditions()}.
	 *
	 * @see #commitDeletions()
	 */
	public void deleteMarker(IMarker marker) {
		assert !closed;
		IMarker dupe = markersToReuse.put(new MarkerSignature(marker), marker);
		if (dupe != null) markersToDelete.add(dupe);
	}
	
	/**
	 * Schedule a marker for deletion. It will not be reused.
	 *
	 * @see #commitDeletions()
	 */
	public void deleteMarkerWithoutReuse(IMarker marker) {
		assert !closed;
		markersToDelete.add(marker);
	}
	
	/**
	 * Schedule a marker for addition.
	 * 
	 * @see #commitAdditions()
	 * @see #commitMultiErrorLineAdditions()
	 * @see #commitReuseableAdditions()
	 */
	public void addMarker(MarkerSignature marker) {
		assert !closed;
		markersToAdd.add(marker);
	}
	
	/**
	 * Clear all markers for the specified resource.
	 * 
	 * @see #commitDeletions()
	 */
	public void clearMarkers(IResource file) {
		assert !closed;
		try {
			for (IMarker marker : file.findMarkers(markerType, true, 0)) {
				deleteMarker(marker);
			}
			for (IMarker marker : file.findMarkers(AstMessageHandler.GENERIC_PROBLEM, false, 0)) {
				// Remove legacy markers (Spoofax/195)
				deleteMarkerWithoutReuse(marker);
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
	 * Protect this batch from any further changes and
	 * enable all commit methods.
	 */
	public void close() {
		closed = true;
	}
	
	/**
	 * Equivalent to {@link #commitReuseableAdditions()}, followed by
	 * {@link #commitAdditions()} and {@link #commitDeletions()}.
	 */
	public void commitAllChanges() {
		assert closed;
		runInWorkspace(new Runnable() {
			public void run() {
				commitReuseableAdditions();
				commitAdditions();
				commitDeletions();
			}
		});
	}
	
	/**
	 * Commits any newly added markers for which existing markers can be reused.
	 * May acquire an editor lock (due to IMarker.setAttributes).
	 * 
	 * This must be called before {@link #commitDeletions()} and
	 * {@link #commitAdditions()} to have any effect.
	 * 
	 * If this method is not called, markers will still be added (but without
	 * reuse of existing markers) by {@link #commitAdditions()}.
	 */
	public void commitReuseableAdditions() {
		assert closed;
		runInWorkspace(new Runnable() {
			public void run() {
				commitReuseableAdditionsInWS();
			}
		});
	}
	
	private void commitReuseableAdditionsInWS() {
		final Iterator<MarkerSignature> markersToAdd = this.markersToAdd.iterator();
		while (markersToAdd.hasNext()) {
			final MarkerSignature signature = markersToAdd.next();
			final IMarker marker = markersToReuse.remove(signature);

			if (marker != null && marker.exists()) {
				try {
					if (!signature.attibutesEqual(marker)) {
						marker.setAttributes(signature.getAttributes(), signature.getValues());
					}
					markersToAdd.remove();
				} catch (CoreException e) {
					Environment.logException("Could not create error marker: " + signature.getMessage(), e);
				}
			}
		}
	}
	
	/**
	 * Deletes markers as instructed using {@link #clearMarkers(IResource)},
	 * {@link #deleteMarker(IMarker)} and {@link #deleteMarkerWithoutReuse(IMarker)}.
	 */
	public void commitDeletions() {
		assert closed;
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
	 * Commit any newly added error markers that are on lines 
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
		return false;
	}
	
	/**
	 * Commit any newly added error markers.
	 * 
	 * @see #addMarker(MarkerSignature)
	 */
	public void commitAdditions() {
		assert closed;
		runInWorkspace(new Runnable() {
			public void run() {
				commitAdditionsInWS();
			}
		});
	}

	private void commitAdditionsInWS() {
		for (MarkerSignature signature : markersToAdd) {
			try {
				IMarker marker = signature.getResource().createMarker(markerType);
				marker.setAttributes(signature.getAttributes(), signature.getValues());
				synchronized (asyncActiveMarkers) {
					asyncActiveMarkers.add(marker);
				}
			} catch (CoreException e) {
				Environment.logException("Could not create error marker: " + signature.getMessage(), e);
			}
		}
		markersToAdd.clear();
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
}
