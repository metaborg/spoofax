package org.metaborg.spoofax.core.language.dialect;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.metaborg.spoofax.core.language.LanguageChange;
import org.metaborg.spoofax.core.resource.IResourceChange;

public interface IDialectProcessor {
    public abstract void loadAll(FileObject directory) throws FileSystemException;

    public abstract void removeAll(FileObject directory) throws FileSystemException;

    public abstract void update(Iterable<IResourceChange> changes);

    public abstract void update(LanguageChange change);
}
