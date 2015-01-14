package org.metaborg.spoofax.eclipse.language;

import org.apache.commons.vfs2.FileObject;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.metaborg.spoofax.core.language.ILanguageDiscoveryService;
import org.metaborg.spoofax.eclipse.SpoofaxPlugin;
import org.metaborg.spoofax.eclipse.resource.IEclipseResourceService;
import org.metaborg.spoofax.eclipse.util.AbstractHandlerUtils;

public class LoadLanguageHandler extends AbstractHandler {
    private final IEclipseResourceService resourceService;
    private final ILanguageDiscoveryService languageDiscoveryService;


    public LoadLanguageHandler() {
        this.resourceService = SpoofaxPlugin.injector().getInstance(IEclipseResourceService.class);
        this.languageDiscoveryService = SpoofaxPlugin.injector().getInstance(ILanguageDiscoveryService.class);
    }


    @Override public Object execute(ExecutionEvent event) throws ExecutionException {
        final IProject project = AbstractHandlerUtils.getProjectFromSelected(event);
        if(project == null)
            return null;

        try {
            final FileObject location = resourceService.resolve(project);
            languageDiscoveryService.discover(location);
        } catch(Exception e) {
            throw new ExecutionException("Cannot load language at given location", e);
        }

        return null;
    }
}
