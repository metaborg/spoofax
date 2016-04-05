package org.metaborg.core.context;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.project.IProject;

public class ProjectContextStrategy implements IContextStrategy {
    public static final String name = "project";

    
    @Override public ContextIdentifier get(FileObject resource, IProject project, ILanguageImpl language)
        throws ContextException {
        return new ContextIdentifier(project.location(), project, language);
    }
}
