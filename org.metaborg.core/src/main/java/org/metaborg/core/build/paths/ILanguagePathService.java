package org.metaborg.core.build.paths;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.project.IProject;

/**
 * Interface for service that returns source/include paths/files for languages.
 */
public interface ILanguagePathService {
    public Iterable<FileObject> sourcePaths(IProject project, String language);

    public Iterable<FileObject> sourceFiles(IProject project, String language);

    public Iterable<FileObject> includePaths(IProject project, String language);

    public Iterable<FileObject> includeFiles(IProject project, String language);

    public Iterable<FileObject> sourceAndIncludePaths(IProject project, String language);

    public Iterable<FileObject> sourceAndIncludeFiles(IProject project, String language);
}
