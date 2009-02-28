package org.strategoxt.imp.metatooling.loading;

import static org.eclipse.core.resources.IResourceDelta.*;
import static org.eclipse.core.resources.IMarker.*;

import java.io.IOException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.strategoxt.imp.metatooling.building.DynamicDescriptorBuilder;
import org.strategoxt.imp.runtime.Environment;
import org.strategoxt.imp.runtime.WorkspaceRunner;
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
	
	private final AstMessageHandler messageHandler =
		new AstMessageHandler();
	
	private DynamicDescriptorBuilder getBuilder() {
		if (builder == null)
			builder = new DynamicDescriptorBuilder(this);
		return builder;
	}

	public void resourceChanged(final IResourceChangeEvent event) {
		if (event.getType() == IResourceChangeEvent.POST_CHANGE) {
			WorkspaceRunner.run(
					new IWorkspaceRunnable() {
							public void run(IProgressMonitor monitor) throws CoreException {
								getBuilder().invalidateUpdatedResources();
								postResourceChanged(event.getDelta());
							}
					});
		}
	}
	
	public void postResourceChanged(IResourceDelta delta) {
		IResourceDelta[] children = delta.getAffectedChildren();
		
		if (children.length == 0) {		
			IResource resource = delta.getResource();
			if ((delta.getFlags() & CONTENT) == CONTENT)
				updateResource(resource);
		} else {
			// Recurse
			for (IResourceDelta child : children)
				postResourceChanged(child);
		}
	}
	
	public void updateResource(IResource resource) {
		if (resource.getName().endsWith(".packed.esv")) {
			loadPackedDescriptor(resource);
		} else {
			getBuilder().updateResource(resource);
		}
	}

	public void loadPackedDescriptor(IResource descriptor) {
		try {
			messageHandler.clearMarkers(descriptor);
			
			IFile file = descriptor.getProject().getFile(descriptor.getProjectRelativePath());
			// UNDONE: Refresh packed descriptor resource if out of sync
			//         (no longer works from workspace thread ... *sigh*)
			// file.refreshLocal(0, null);
			DescriptorFactory.load(file);
			
		} catch (CoreException e) {
			Environment.logException("Unable to load descriptor " + descriptor, e);
			messageHandler.addMarkerFirstLine(descriptor, "Unable to load descriptor: " + e.getMessage(), SEVERITY_ERROR);
		} catch (BadDescriptorException e) {
			Environment.logException("Error in descriptor " + descriptor, e);
			messageHandler.addMarkerFirstLine(descriptor, "Error in descriptor: " + e.getMessage(), SEVERITY_ERROR);
		} catch (IOException e) {
			Environment.logException("Error reading descriptor " + descriptor, e);
			messageHandler.addMarkerFirstLine(descriptor, "Error reading descriptor: " + e.getMessage(), SEVERITY_ERROR);
		} catch (RuntimeException e) {
			Environment.logException("Unable to load descriptor " + descriptor, e);
			messageHandler.addMarkerFirstLine(descriptor, "Unable to load descriptor: " + e.getMessage(), SEVERITY_ERROR);
		} catch (Error e) { // workspace thread swallows this >:(
			Environment.logException("Unable to load descriptor " + descriptor, e);
			messageHandler.addMarkerFirstLine(descriptor, "Unable to load descriptor: " + e.getMessage(), SEVERITY_ERROR);
			throw e;
		}
	}
}
