package org.strategoxt.imp.runtime.dynamicloading;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.strategoxt.imp.runtime.Environment;

import com.google.inject.Inject;

/**
 * This class loads all active descriptors in the workspace at startup, and activates the
 * {@link DynamicDescriptorLoader} class.
 */
public class StartupDescriptorLoader {
    private final DynamicDescriptorLoader loader;

    @Inject public StartupDescriptorLoader(DynamicDescriptorLoader loader) {
        this.loader = loader;
    }

    /**
     * Initializes the dynamic language loading component.
     */
    public void run() {
        try {
            loadAllServices();

            // (note: don't set eventMask parameter; Eclipse will ignore some events)
            ResourcesPlugin.getWorkspace().addResourceChangeListener(loader);

        } catch(RuntimeException e) {
            Environment.logException("Could not load dynamic descriptor updater", e);
        }
    }

    private void loadAllServices() {
        Environment.getStrategoLock().lock();
        try {
            for(final IProject project : ResourcesPlugin.getWorkspace().getRoot().getProjects()) {
                if(project.isOpen()) {
                    try {
                        project.accept(new IResourceVisitor() {
                            public boolean visit(IResource resource) throws CoreException {
                                loader.updateResource(resource, new NullProgressMonitor(), true);
                                return true;
                            }
                        });
                    } catch(CoreException e) {
                        Environment.logException("Error loading descriptors for project " + project.getName(), e);
                    }
                }
            }
        } finally {
            Environment.getStrategoLock().unlock();
        }
    }
}
