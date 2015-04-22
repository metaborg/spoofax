package org.metaborg.spoofax.core.context;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.spoofax.core.language.ILanguage;
import org.metaborg.spoofax.core.project.IProject;
import org.metaborg.spoofax.core.project.IProjectService;

import com.google.inject.Inject;

public class ProjectContextStrategy implements IContextStrategy {
    private static final long serialVersionUID = -3941929496884299255L;

    public static final String name = "project";

    private final IProjectService projectService;


    @Inject public ProjectContextStrategy(IProjectService projectService) {
        this.projectService = projectService;
    }


    @Override public ContextIdentifier get(FileObject resource, ILanguage language) throws ContextException {
        final IProject project = projectService.get(resource);
        if(project == null) {
            final String message =
                String.format("Cannot create or retrieve context, %s does not have a project", resource);
            throw new ContextException(resource, language, message);
        }
        return new ContextIdentifier(project.location(), language);
    }
}
