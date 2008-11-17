package org.strategoxt.imp.metatooling;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
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
	
	/**
	 * Initializes the dynamic language loading component.
	 * May be invoked by {@link DynamicLanguageValidator }
	 */
	public static void initialize() {
		if (didInitialize) return;
		
		didInitialize = true;
		
		ResourcesPlugin.getWorkspace().addResourceChangeListener(loader);
		try {
			ResourcesPlugin.getWorkspace().run(
				new IWorkspaceRunnable() {
					public void run(IProgressMonitor monitor) throws CoreException {
						loadAllServices();
					}},
				null);
		} catch (CoreException e) {
			Environment.logException("Could not load initial editor services");
		}
	}
	
	/* TODO: Load only descriptors indicated in the project settings
	
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
					project.accept(new IResourceVisitor() {
						public boolean visit(IResource resource)
								throws CoreException {
							
							if (resource.getName().endsWith(".packed.esv"))
								loader.loadDescriptor(project, resource);
	
							return true;
						}
					});
				} catch (CoreException e) {
					Environment.logException("Error loading descriptors for project " + project.getName(), e);
				}
			}
		}
	}
}
