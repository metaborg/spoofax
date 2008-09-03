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
public class StartupDescriptorLoader implements IStartup {
	private static boolean didInitialize;
	
	private static final DynamicDescriptorLoader loader = new DynamicDescriptorLoader();

	@Override
	public void earlyStartup() {
		initialize();
	}
	
	public void initialize() {
		if (didInitialize) return;
		
		didInitialize = true;
		
		ResourcesPlugin.getWorkspace().addResourceChangeListener(loader);
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
	
	private void loadInitialServices() {
		for (IProject project : ResourcesPlugin.getWorkspace().getRoot().getProjects()) {
			if (project.isOpen()) {
				for (String descriptor : LoaderPreferences.get(project).getDescriptors()) {
					loader.loadDescriptor(project, descriptor);
				}
			}
		}
	}
}
