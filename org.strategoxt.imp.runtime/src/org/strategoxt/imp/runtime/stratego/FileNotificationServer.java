package org.strategoxt.imp.runtime.stratego;

import static org.eclipse.core.resources.IResourceChangeEvent.POST_CHANGE;
import static org.eclipse.core.resources.IResourceChangeEvent.PRE_CLOSE;
import static org.eclipse.core.resources.IResourceChangeEvent.PRE_DELETE;
import static org.eclipse.core.resources.IResourceDelta.ADDED;
import static org.eclipse.core.resources.IResourceDelta.CHANGED;
import static org.eclipse.core.resources.IResourceDelta.CONTENT;
import static org.eclipse.core.resources.IResourceDelta.MOVED_FROM;
import static org.eclipse.core.resources.IResourceDelta.MOVED_TO;
import static org.eclipse.core.resources.IResourceDelta.NO_CHANGE;
import static org.eclipse.core.resources.IResourceDelta.REMOVED;
import static org.eclipse.core.resources.IResourceDelta.REPLACED;

import java.net.URI;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.imp.language.LanguageRegistry;
import org.spoofax.interpreter.library.index.notification.NotificationCenter;
import org.strategoxt.imp.runtime.Environment;

/**
 * Sends Eclipse resource change notifications to the Spoofax {@link NotificationCenter}.
 * 
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class FileNotificationServer implements IResourceChangeListener {
	private boolean projectClosing = false;

	private FileNotificationServer() {
		// Use the statics
	}

	public static void init() {
		// (note: don't set eventMask parameter; Eclipse will ignore some events)
		ResourcesPlugin.getWorkspace().addResourceChangeListener(new FileNotificationServer());

		NotificationCenter.putObserver(null, null, new QueueAnalysisService());
	}

	public void resourceChanged(IResourceChangeEvent event) {
		switch(event.getType()) {
			case PRE_CLOSE:
			case PRE_DELETE:
				projectClosing = true;
				break;
			case POST_CHANGE: {
				if(!projectClosing)
					postResourceChanged(event.getDelta());
				projectClosing = false;
				break;
			}
		}
	}

	private void postResourceChanged(IResourceDelta delta) {
		try {
			final List<URI> changedFiles = new LinkedList<URI>();

			delta.accept(new IResourceDeltaVisitor() {
				public boolean visit(IResourceDelta delta) throws CoreException {
					if((delta.getFlags() & IResourceDelta.OPEN) == IResourceDelta.OPEN) {
						return false;
					}

					IResource resource = delta.getResource();
					if(isSignificantChange(delta) && !isIgnoredChange(resource) && resource.getLocation() != null
						&& LanguageRegistry.findLanguage(resource.getLocation(), null) != null) {
						changedFiles.add(resource.getLocationURI());
					}
					return true;
				}
			});

			if(changedFiles.size() > 0)
				NotificationCenter.notifyFileChanges(changedFiles, false);
		} catch(CoreException e) {
			Environment.logException("Exception when processing fileystem events", e);
		}
	}

	public static boolean isSignificantChange(IResourceDelta delta) {
		switch(delta.getKind()) {
			case ADDED:
			case REMOVED:
				return true;
			case CHANGED:
				final int flags = delta.getFlags();
				return (flags & CONTENT) == CONTENT || (flags & MOVED_TO) == MOVED_TO
					|| (flags & MOVED_FROM) == MOVED_FROM || (flags & REPLACED) == REPLACED;
			case NO_CHANGE:
				return false;
			default:
				assert false : "Unknown state";
				return false;
		}
	}

	private static boolean isIgnoredChange(IResource resource) {
		IPath path = resource.getProjectRelativePath();
		if(path.segmentCount() > 1) {
			String base = path.segment(0);
			if(".cache".equals(base) || ".shadowdir".equals(base) || "include".equals(base) || "bin".equals(base)
				|| ".spxcache".equals(base) || "__MACOS_X".equals(base))
				return true;
		}
		return false;
	}
}
