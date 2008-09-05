package org.strategoxt.imp.metatooling;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.strategoxt.imp.runtime.Environment;

/**
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class StartupDescriptorLoader {
	private static boolean didInitialize;
	
	private static final DynamicDescriptorLoader loader = new DynamicDescriptorLoader();
	
	public static void initialize() {
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
	
	private static void loadInitialServices() {
		for (IProject project : ResourcesPlugin.getWorkspace().getRoot().getProjects()) {
			if (project.isOpen()) {
				for (String descriptor : LoaderPreferences.get(project).getDescriptors()) {
					loader.loadDescriptor(project, descriptor);
				}
			}
		}
	}
}
