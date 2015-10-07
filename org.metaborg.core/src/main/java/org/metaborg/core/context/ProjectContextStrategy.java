package org.metaborg.core.context;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.project.IProject;
import org.metaborg.core.project.IProjectService;

import com.google.inject.Inject;

public class ProjectContextStrategy implements IContextStrategy {
    public static final String name = "project";

    private final IProjectService projectService;


    @Inject public ProjectContextStrategy(IProjectService projectService) {
        this.projectService = projectService;
    }


    @Override public ContextIdentifier get(FileObject resource, ILanguageImpl language) throws ContextException {
        final IProject project = projectService.get(resource);
        if(project == null) {
            final String message =
                String.format("Cannot create or retrieve context, %s does not have a project", resource);
            throw new ContextException(resource, language, message);
        }
        return new ContextIdentifier(project.location(), language);
    }
}
