package org.strategoxt.imp.metatooling.loading;

import static org.eclipse.core.resources.IMarker.*;
import static org.eclipse.core.resources.IResourceDelta.*;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.imp.metatooling.building.DynamicDescriptorBuilder;
import org.strategoxt.imp.runtime.Environment;
import org.strategoxt.imp.runtime.dynamicloading.BadDescriptorException;
import org.strategoxt.imp.runtime.dynamicloading.DescriptorFactory;
import org.strategoxt.imp.runtime.parser.ast.AstMessageHandler;

/**
 * This class updates any editors in the environment,
 * triggered by resource change events.
 * 
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class DynamicDescriptorUpdater implements IResourceChangeListener {
	
	private DynamicDescriptorBuilder builder;
	
	private static Set<IResource> updateOnly =
		Collections.synchronizedSet(new HashSet<IResource>());
	
	// TODO: Use (and properly clean up) new marker type for internal errors?
	//       (also seen in DynamicDescriptorBuilder)
	private final AstMessageHandler messageHandler =
		new AstMessageHandler(AstMessageHandler.ANALYSIS_MARKER_TYPE);
	
	private DynamicDescriptorBuilder getBuilder() {
		if (builder == null)
			builder = new DynamicDescriptorBuilder(this);
		return builder;
	}
	
	/**
	 * Schedules a file to be updated (not built)
	 * for the next time a matching resource event
	 * is received.
	 */
	public static void scheduleUpdate(IResource resource) {
		assert resource.toString().endsWith(".packed.esv");
		updateOnly.add(resource);
	}

	public void resourceChanged(final IResourceChangeEvent event) {
		if (event.getType() == IResourceChangeEvent.POST_CHANGE) {
			Job job = new WorkspaceJob("Updating editor descriptor runtime") {
				@Override
				public IStatus runInWorkspace(IProgressMonitor monitor) {
					// TODO: Finer-grained locking?
					synchronized (Environment.getSyncRoot()) {
						monitor.beginTask("", IProgressMonitor.UNKNOWN);
						postResourceChanged(event.getDelta(), monitor);
						return Status.OK_STATUS;
					}
				}
			};
			job.setRule(event.getResource());
			job.schedule();
		}
	}
	
	public void postResourceChanged(IResourceDelta delta, IProgressMonitor monitor) {
		IResourceDelta[] children = delta.getAffectedChildren();
		
		if (children.length == 0) {		
			IResource resource = delta.getResource();
			if (isSignificantChange(delta))
				updateResource(resource, monitor, false);
		} else {
			// Recurse
			for (IResourceDelta child : children)
				postResourceChanged(child, monitor);
		}
	}

	private static boolean isSignificantChange(IResourceDelta delta) {
		int flags = delta.getFlags();
		return (flags & CONTENT) == CONTENT
			|| (flags & MOVED_TO) == MOVED_TO
			|| (flags & MOVED_FROM) == MOVED_FROM
			|| (flags & REPLACED) == REPLACED
			|| (flags == 0);
	}
	
	public void updateResource(IResource resource, IProgressMonitor monitor, boolean startup) {
		if (resource.getName().endsWith(".packed.esv")) {
			IResource source = getMainDescriptor(resource);
			
			boolean isUpdateOnly = updateOnly.contains(resource);
			
			if (!source.equals(resource) && !isUpdateOnly) {
				// Try to build using the .main.esv instead;
				// the build.xml file may touch the .packed.esv file
				// to signal a rebuild is necessary
				getBuilder().updateResource(source, monitor);
			} else if (resource.exists()) {
				monitor.subTask("Loading " + resource.getName());
				loadPackedDescriptor(resource);
				if (isUpdateOnly) updateOnly.remove(resource);
			}
		} else if (!startup) {
			// Re-build descriptor if resource changed (but not if we're starting up)
			getBuilder().updateResource(resource, monitor);
		}
	}

	public void loadPackedDescriptor(IResource descriptor) {
		// TODO2: Properly trace back descriptor errors to their original source
		IResource source = getMainDescriptor(descriptor);
		try {
			messageHandler.clearMarkers(source);
			
			IFile file = descriptor.getProject().getFile(descriptor.getProjectRelativePath());
			DescriptorFactory.load(file);
			
		} catch (BadDescriptorException e) {
			reportError(descriptor, e.getOffendingTerm(), "Error in descriptor: " + e.getMessage());
		} catch (IOException e) {
			Environment.logException("Error reading descriptor " + descriptor, e);
			reportError(source, "Internal error reading descriptor" + e.getMessage());
		} catch (CoreException e) {
			Environment.logException("Unable to load descriptor " + descriptor, e);
			reportError(source, "Internal error loading descriptor: " + e.getMessage());
		} catch (RuntimeException e) {
			Environment.logException("Unable to load descriptor " + descriptor, e);
			reportError(source, "Internal error loading descriptor: " + e.getMessage());
		} catch (Error e) { // workspace thread swallows this >:(
			Environment.logException("Unable to load descriptor " + descriptor, e);
			reportError(source, "Internal error loading descriptor: " + e.getMessage());
			throw e;
		}
	}
	
	private IResource getMainDescriptor(IResource packedDescriptor) {
		String name = packedDescriptor.getName();
		name = name.substring(0, name.length() - ".packed.esv".length());
		IResource result = packedDescriptor.getParent().getParent().findMember("editor/" + name + ".main.esv");
		return result != null ? result : packedDescriptor;
	}
	
	private void reportError(final IResource descriptor, final String message) {
		if (ResourcesPlugin.getWorkspace().isTreeLocked()) {
			Job job = new WorkspaceJob("Add error marker") {
				{ setSystem(true); } // don't show to user
				@Override
				public IStatus runInWorkspace(IProgressMonitor monitor) {
					messageHandler.addMarkerFirstLine(descriptor, message, SEVERITY_ERROR);
					return Status.OK_STATUS;
				}
			};
			job.setRule(descriptor);
			job.schedule();
		} else {
			messageHandler.addMarkerFirstLine(descriptor, message, SEVERITY_ERROR);
		}
	}
	
	private void reportError(final IResource descriptor, final IStrategoTerm offendingTerm, final String message) {
		if (ResourcesPlugin.getWorkspace().isTreeLocked()) {
			Job job = new WorkspaceJob("Add error marker") {
				{ setSystem(true); } // don't show to user
				@Override
				public IStatus runInWorkspace(IProgressMonitor monitor) {
					messageHandler.addMarker(descriptor, offendingTerm, message, SEVERITY_ERROR);
					return Status.OK_STATUS;
				}
			};
			job.setRule(descriptor);
			job.schedule();
		} else {
			messageHandler.addMarkerFirstLine(descriptor, message, SEVERITY_ERROR);
		}
	}
}
