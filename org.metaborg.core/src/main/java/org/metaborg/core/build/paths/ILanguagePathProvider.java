package org.metaborg.core.build.paths;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.project.IProject;

/**
 * Interface for providers for source and include paths.
 */
public interface ILanguagePathProvider {
    public Iterable<FileObject> sourcePaths(IProject project, String languageName);

    public Iterable<FileObject> includePaths(IProject project, String languageName);
}
