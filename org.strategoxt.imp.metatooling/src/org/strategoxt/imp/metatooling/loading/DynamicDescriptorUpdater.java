package org.strategoxt.imp.metatooling.loading;

import java.io.IOException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.runtime.CoreException;
import org.spoofax.interpreter.core.InterpreterException;
import org.strategoxt.imp.metatooling.building.DynamicDescriptorBuilder;
import org.strategoxt.imp.runtime.Environment;
import org.strategoxt.imp.runtime.dynamicloading.BadDescriptorException;
import org.strategoxt.imp.runtime.dynamicloading.DescriptorFactory;

/**
 * This class updates any editors in the environment,
 * triggered by resource change events.
 * 
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class DynamicDescriptorUpdater implements IResourceChangeListener {
	
	private final DynamicDescriptorBuilder builder;
	
	public DynamicDescriptorUpdater() throws InterpreterException, IOException {
		builder = new DynamicDescriptorBuilder(this);
	}

	public void resourceChanged(IResourceChangeEvent event) {
		if (event.getType() == IResourceChangeEvent.POST_CHANGE) {
			postResourceChanged(event.getDelta());
		}
	}
	
	public void postResourceChanged(IResourceDelta delta) {
		IResourceDelta[] children = delta.getAffectedChildren();
		
		if (children.length == 0) {		
			IResource resource = delta.getResource();
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
			builder.updateResource(resource);
		}
	}

	public void loadPackedDescriptor(IResource descriptor) {
		try {
			IFile file = descriptor.getProject().getFile(descriptor.getProjectRelativePath());
			file.refreshLocal(0, null); // resource might be out of sync
			DescriptorFactory.load(file);
			
		} catch (CoreException e) {
			Environment.logException("Unable to load descriptor " + descriptor, e);
		} catch (BadDescriptorException e) {
			// TODO: Properly report bad descriptors in the UI
			Environment.logException("Error in descriptor " + descriptor, e);
		} catch (IOException e) {
			Environment.logException("Error reading descriptor " + descriptor, e);
		} catch (RuntimeException e) {
			Environment.logException("Unable to load descriptor " + descriptor, e);
		} catch (Error e) { // workspace thread swallows this >:(
			Environment.logException("Unable to load descriptor " + descriptor, e);
			throw e;
		}
	}
}
