package org.strategoxt.imp.metatooling.loading;

import static org.eclipse.core.resources.IMarker.*;
import static org.eclipse.core.resources.IResourceDelta.*;

import java.io.IOException;

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
	
	// TODO: Use (and properly clean up) new marker type for internal errors?
	private final AstMessageHandler messageHandler =
		new AstMessageHandler(AstMessageHandler.ANALYSIS_MARKER_TYPE);
	
	private DynamicDescriptorBuilder getBuilder() {
		if (builder == null)
			builder = new DynamicDescriptorBuilder(this);
		return builder;
	}

	public void resourceChanged(final IResourceChangeEvent event) {
		if (event.getType() == IResourceChangeEvent.POST_CHANGE) {
			Job job = new WorkspaceJob("Updating editor descriptor runtime") {
				@Override
				public IStatus runInWorkspace(IProgressMonitor monitor) {
					// TODO: Finer-grained locking?
					synchronized (Environment.getSyncRoot()) {
						monitor.beginTask("", IProgressMonitor.UNKNOWN);
						getBuilder().invalidateUpdatedResources();
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
		// FIXME: Enqueue all updates, ensure builder runs first
		// FIXME: The builder should refresh any build resources (i.e., .packed.esv)
		if (resource.getName().endsWith(".packed.esv")) {
			monitor.subTask("Loading " + resource.getName());
			loadPackedDescriptor(resource);
		} else if (!startup) {
			// Re-build descriptor if resource changed (but not if we're starting up)
			getBuilder().updateResource(resource, monitor);
		}
	}

	public void loadPackedDescriptor(IResource descriptor) {
		try {
			messageHandler.clearMarkers(descriptor);
			
			IFile file = descriptor.getProject().getFile(descriptor.getProjectRelativePath());
			DescriptorFactory.load(file);
			
		} catch (BadDescriptorException e) {
			reportError(descriptor, e.getOffendingTerm(), "Error in descriptor: " + e.getMessage());
		} catch (IOException e) {
			Environment.logException("Error reading descriptor " + descriptor, e);
			reportError(descriptor, "Internal error reading descriptor" + e.getMessage());
		} catch (CoreException e) {
			Environment.logException("Unable to load descriptor " + descriptor, e);
			reportError(descriptor, "Internal error loading descriptor: " + e.getMessage());
		} catch (RuntimeException e) {
			Environment.logException("Unable to load descriptor " + descriptor, e);
			reportError(descriptor, "Internal error loading descriptor: " + e.getMessage());
		} catch (Error e) { // workspace thread swallows this >:(
			Environment.logException("Unable to load descriptor " + descriptor, e);
			reportError(descriptor, "Internal error loading descriptor: " + e.getMessage());
			throw e;
		}
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
