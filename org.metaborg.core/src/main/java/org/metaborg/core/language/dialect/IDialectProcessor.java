package org.metaborg.core.language.dialect;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.metaborg.core.language.LanguageChange;
import org.metaborg.core.resource.ResourceChange;

public interface IDialectProcessor {
    public abstract void loadAll(FileObject directory) throws FileSystemException;

    public abstract void removeAll(FileObject directory) throws FileSystemException;

    public abstract void update(Iterable<ResourceChange> changes);

    public abstract void update(LanguageChange change);
}
