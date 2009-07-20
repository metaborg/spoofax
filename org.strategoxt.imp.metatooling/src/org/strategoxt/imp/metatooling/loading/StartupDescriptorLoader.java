package org.strategoxt.imp.metatooling.loading;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.strategoxt.imp.runtime.Environment;

/**
 * This class loads all active descriptors  in the workspace at startup,
 * and activates the <ref>DynamicDescriptorUpdater</ref> class.
 * 
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class StartupDescriptorLoader {
	private static boolean didInitialize;
	
	private static DynamicDescriptorUpdater loader;
	
	private StartupDescriptorLoader() {}
	
	/**
	 * Initializes the dynamic language loading component.
	 * May be invoked by {@link StartupDescriptorValidator}
	 */
	public static void run() {
		try {
			if (didInitialize) return;
			didInitialize = true;
			
			loader = new DynamicDescriptorUpdater();
		
			loadAllServices();
			
			ResourcesPlugin.getWorkspace().addResourceChangeListener(loader);
			
		} catch (RuntimeException e) {
			Environment.logException("Could not load dynamic descriptor updater", e);
		}
	}
	
	/* TODO: Only load descriptors indicated in the project settings
	
	private static void loadProjectSettingsServices() {
		for (IProject project : ResourcesPlugin.getWorkspace().getRoot().getProjects()) {
			if (project.isOpen()) {
				for (String descriptor : LoaderPreferences.get(project).getDescriptors()) {
					loader.loadDescriptor(project, descriptor);
				}
			}
		}
	}
	*/
	
	private static void loadAllServices() {
		for (final IProject project : ResourcesPlugin.getWorkspace().getRoot().getProjects()) {
			if (project.isOpen()) {
				try {
					synchronized (Environment.getSyncRoot()) {
						project.accept(new IResourceVisitor() {
							public boolean visit(IResource resource) throws CoreException {
								loader.updateResource(resource, new NullProgressMonitor(), true);
								return true;
							}
						});
					}
				} catch (CoreException e) {
					Environment.logException("Error loading descriptors for project " + project.getName(), e);
				}
			}
		}
	}
}
