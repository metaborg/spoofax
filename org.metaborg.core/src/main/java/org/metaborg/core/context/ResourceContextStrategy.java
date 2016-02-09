package org.metaborg.core.context;

import com.google.inject.Inject;
import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.project.ILanguageSpec;
import org.metaborg.core.project.IProject;
import org.metaborg.core.project.IProjectService;

public class ResourceContextStrategy implements IContextStrategy {
    public static final String name = "resource";

    @Override public ContextIdentifier get(FileObject resource, IProject project, ILanguageImpl language) {
        return new ContextIdentifier(resource, project, language);
    }
}
