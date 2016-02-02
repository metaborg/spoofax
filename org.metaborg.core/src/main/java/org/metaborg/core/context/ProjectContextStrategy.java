package org.metaborg.core.context;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.project.ILanguageSpec;
import org.metaborg.core.project.IProject;
import org.metaborg.core.project.IProjectService;

import com.google.inject.Inject;

public class ProjectContextStrategy implements IContextStrategy {
    public static final String name = "project";

    @Override public ContextIdentifier get(FileObject resource, ILanguageSpec project, ILanguageImpl language) throws ContextException {
        return new ContextIdentifier(project.location(), language);
    }
}
