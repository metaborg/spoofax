package org.metaborg.meta.core.project;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.project.Project;

/**
 * A language specification project.
 */
public class LanguageSpecProject extends Project implements ILanguageSpec {
    public LanguageSpecProject(final FileObject location) {
        super(location);
    }
}
