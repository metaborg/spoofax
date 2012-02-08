package org.strategoxt.imp.runtime.stratego;

import static org.eclipse.core.resources.IResourceChangeEvent.POST_CHANGE;
import static org.eclipse.core.resources.IResourceDelta.ADDED;
import static org.eclipse.core.resources.IResourceDelta.CHANGED;
import static org.eclipse.core.resources.IResourceDelta.CONTENT;
import static org.eclipse.core.resources.IResourceDelta.MOVED_FROM;
import static org.eclipse.core.resources.IResourceDelta.MOVED_TO;
import static org.eclipse.core.resources.IResourceDelta.REMOVED;
import static org.eclipse.core.resources.IResourceDelta.REPLACED;

import java.net.URI;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.imp.language.LanguageRegistry;
import org.spoofax.interpreter.library.language.NotificationCenter;
import org.strategoxt.imp.runtime.Environment;

/**
 * Sends Eclipse resource change notifications
 * to the Spoofax {@link NotificationCenter}.
 * 
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class FileNotificationServer implements IResourceChangeListener {
	
	public static final int SIGNIFICANT_CHANGES =
			CONTENT | MOVED_TO | MOVED_FROM | REPLACED | ADDED |
			ADDED | REMOVED | CHANGED;
	
	private FileNotificationServer() {
		// Use the statics
	}

	public static void init() {
		ResourcesPlugin.getWorkspace().addResourceChangeListener(new FileNotificationServer(), SIGNIFICANT_CHANGES);

		NotificationCenter.putObserver(null, null, new QueueAnalysisService());
	}

	public void resourceChanged(IResourceChangeEvent event) {
		if (event.getType() == POST_CHANGE) {
			postResourceChanged(event.getDelta());
		}
	}

	private void postResourceChanged(IResourceDelta delta) {
		try {
			delta.accept(new IResourceDeltaVisitor() {
				public boolean visit(IResourceDelta delta) throws CoreException {
					IResource resource = delta.getResource();
					if (isSignificantChange(delta) && LanguageRegistry.findLanguage(resource.getLocation(), null) != null) {
						URI uri = resource.getLocationURI();
						NotificationCenter.notifyChanges(uri, null);
					}
					return true;
				}
			});
		} catch (CoreException e) {
			Environment.logException("Exception when processing fileystem events", e);
		}
	}

	public static boolean isSignificantChange(IResourceDelta delta) {
		int flags = delta.getFlags();
		return (flags & ADDED) == ADDED
			|| (flags & REMOVED) == REMOVED
			|| (flags & CHANGED) == CHANGED
			|| (flags & CONTENT) == CONTENT
			|| (flags & MOVED_TO) == MOVED_TO
			|| (flags & MOVED_FROM) == MOVED_FROM
			|| (flags & REPLACED) == REPLACED;
	}
}
