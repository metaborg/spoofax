package org.metaborg.spoofax.eclipse.language;

import org.apache.commons.vfs2.FileObject;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.metaborg.spoofax.core.language.ILanguageDiscoveryService;
import org.metaborg.spoofax.eclipse.resource.IEclipseResourceService;

import com.google.inject.Inject;

public class StartupLanguageLoader {
    private final IEclipseResourceService resourceService;
    private final ILanguageDiscoveryService languageDiscoveryService;


    @Inject public StartupLanguageLoader(IEclipseResourceService resourceService,
        ILanguageDiscoveryService languageDiscoveryService) {
        this.resourceService = resourceService;
        this.languageDiscoveryService = languageDiscoveryService;
    }


    public void loadLanguages() {
        for(final IProject project : ResourcesPlugin.getWorkspace().getRoot().getProjects()) {
            if(project.isOpen()) {
                try {
                    final FileObject resource = resourceService.resolve(project);
                    languageDiscoveryService.discover(resource);
                } catch(Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }
}
