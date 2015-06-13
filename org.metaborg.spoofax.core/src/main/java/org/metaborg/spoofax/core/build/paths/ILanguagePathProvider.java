package org.metaborg.spoofax.core.build.paths;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.spoofax.core.project.IProject;

public interface ILanguagePathProvider {
    public Iterable<FileObject> sources(IProject project, String language);

    public Iterable<FileObject> includes(IProject project, String language);
}
