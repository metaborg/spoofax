package org.strategoxt.imp.metatooling.loading;

import java.io.IOException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.spoofax.NotImplementedException;
import org.strategoxt.imp.runtime.Environment;
import org.strategoxt.imp.runtime.dynamicloading.BadDescriptorException;
import org.strategoxt.imp.runtime.dynamicloading.DescriptorFactory;

/**
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class DynamicDescriptorLoader implements IResourceChangeListener {
	
	// TODO: Reloading should trigger for dependant files, attachments, etc.

	public void resourceChanged(IResourceChangeEvent event) {
		if (event.getType() == IResourceChangeEvent.POST_CHANGE) {
			postResourceChanged(event.getDelta());
		}
	}
	
	public void postResourceChanged(IResourceDelta delta) {
		if (delta.getResource().getName().endsWith(".packed.esv")) {
			switch (delta.getKind()) {
				case IResourceDelta.ADDED:
					descriptorAdded(delta.getResource());
					break;
				case IResourceDelta.CHANGED:
					descriptorChanged(delta.getResource());
					break;
				case IResourceDelta.REMOVED:
					descriptorRemoved(delta.getResource());
					break;
			}
		}
		for (IResourceDelta child : delta.getAffectedChildren()) {
			postResourceChanged(child);
		}
	}
	
	public void loadDescriptor(IProject project, IResource descriptor) {
		loadDescriptor(project, getMainDescriptorLocation(descriptor));
	}
	
	public void loadDescriptor(IProject project, String descriptor) {
		try {
			IFile file = project.getFile(descriptor);
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
	
	private void descriptorAdded(IResource descriptor) {
		String path = getMainDescriptorLocation(descriptor);
		LoaderPreferences.get(descriptor.getProject()).putDescriptor(path);
		loadDescriptor(descriptor.getProject(), path);
	}
	
	private void descriptorChanged(IResource descriptor) {
		String path = getMainDescriptorLocation(descriptor);
		if (LoaderPreferences.get(descriptor.getProject()).hasDescriptor(path))
			loadDescriptor(descriptor.getProject(), path);
	}
	
	private void descriptorRemoved(IResource descriptor) {
		String path = getMainDescriptorLocation(descriptor);
		if (descriptor.getLocation().toString().equals(path)) {
			LoaderPreferences.get(descriptor.getProject()).removeDescriptor(path);
			// TODO: Unload descriptor
			throw new NotImplementedException("Descriptor unloading");
		} else {
			descriptorChanged(descriptor);
		}
	}

	private String getMainDescriptorLocation(IResource descriptor) {
		// TODO: Get the path of the main descriptor file(s?) associated with this resource
		IPath result = descriptor.getProjectRelativePath();
		return result.toString();
	}
}
