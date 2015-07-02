package org.metaborg.core.build.paths;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.project.IProject;

public interface ILanguagePathService {
    public Iterable<FileObject> sources(IProject project, String language);

    public Iterable<FileObject> includes(IProject project, String language);

    public Iterable<FileObject> sourcesAndIncludes(IProject project, String language);
}
