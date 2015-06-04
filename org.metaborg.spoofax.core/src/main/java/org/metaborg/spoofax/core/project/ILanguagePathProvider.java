package org.metaborg.spoofax.core.project;

import org.apache.commons.vfs2.FileObject;

public interface ILanguagePathProvider {
    public Iterable<FileObject> sources(IProject project, String language);
    public Iterable<FileObject> includes(IProject project, String language);
}
