package org.strategoxt.imp.metatooling;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.IStartup;
import org.spoofax.NotImplementedException;
import org.strategoxt.imp.runtime.Environment;
import org.strategoxt.imp.runtime.dynamicloading.BadDescriptorException;
import org.strategoxt.imp.runtime.dynamicloading.DescriptorFactory;

/**
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class DynamicDescriptorLoader implements IResourceChangeListener, IStartup {
	public void earlyStartup() {
		ResourcesPlugin.getWorkspace().addResourceChangeListener(this);
		try {
			ResourcesPlugin.getWorkspace().run(
				new IWorkspaceRunnable() {
					@Override
					public void run(IProgressMonitor monitor) throws CoreException {
						loadInitialServices();
					}},
				null);
		} catch (CoreException e) {
			Environment.logException("Could not load initial editor services");
		}
	}

	@Override
	public void resourceChanged(IResourceChangeEvent event) {
		if (event.getType() == IResourceChangeEvent.POST_CHANGE) {
			for (IResourceDelta delta : event.getDelta().getAffectedChildren()) {
				if (delta.getResource().getFileExtension().equals("esv")) {
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
			}
			for (IResourceDelta added : event.getDelta().getAffectedChildren()) {
				if (added.getResource().getFileExtension().equals("esv")) {
					descriptorAdded(added.getResource());
				}
			}
		}
	}
	
	private void loadInitialServices() {
		for (IProject project : ResourcesPlugin.getWorkspace().getRoot().getProjects()) {
			for (String descriptor : LoaderPreferences.get(project).getDescriptors()) {
				loadDescriptor(project, descriptor);
			}
		}
	}
	
	private void loadDescriptor(IProject project, String relativePath) {
		try {
			String path = project.getRawLocation().append(relativePath).toString();
			InputStream stream = new BufferedInputStream(new FileInputStream(path));
			DescriptorFactory.load(stream);
		} catch (FileNotFoundException e) {
			Environment.logException("Unable to load descriptor");
		} catch (BadDescriptorException e) {
			// TODO: Report bad descriptors in the UI
			Environment.logException("Error in descriptor " + relativePath, e);
		}
	}
	
	private void descriptorAdded(IResource descriptor) {
		String path = getMainDescriptorLocation(descriptor);
		loadDescriptor(descriptor.getProject(), path);
		LoaderPreferences.get(descriptor.getProject()).putDescriptor(path);
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
		return descriptor.getLocation().toString();
	}
}
