package org.strategoxt.imp.metatooling;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
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
			postResourceChanged(event.getDelta());
		}
	}
	
	public void postResourceChanged(IResourceDelta delta) {
		if ("esv".equals(delta.getResource().getFileExtension())) {
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
	
	private void loadInitialServices() {
		for (IProject project : ResourcesPlugin.getWorkspace().getRoot().getProjects()) {
			if (project.isOpen()) {
				for (String descriptor : LoaderPreferences.get(project).getDescriptors()) {
					loadDescriptor(project, descriptor);
				}
			}
		}
	}
	
	public void loadDescriptor(IProject project, String path) {
		try {
			IFile file = project.getFile(path);
			DescriptorFactory.load(file);
		} catch (CoreException e) {
			Environment.logException("Unable to load descriptor " + path, e);
		} catch (BadDescriptorException e) {
			// TODO: Report bad descriptors in the UI
			Environment.logException("Error in descriptor " + path, e);
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
