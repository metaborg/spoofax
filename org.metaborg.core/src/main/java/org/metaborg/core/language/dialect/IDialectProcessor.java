package org.metaborg.core.language.dialect;

import org.apache.commons.vfs2.FileSystemException;
import org.metaborg.core.language.LanguageChange;
import org.metaborg.core.project.IProject;
import org.metaborg.core.resource.ResourceChange;

public interface IDialectProcessor {
    public abstract void loadAll(IProject project) throws FileSystemException;

    public abstract void removeAll(IProject project) throws FileSystemException;

    public abstract void update(IProject project, Iterable<ResourceChange> changes);

    public abstract void update(LanguageChange change);
}
