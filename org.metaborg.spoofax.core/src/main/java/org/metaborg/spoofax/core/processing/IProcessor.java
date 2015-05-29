package org.metaborg.spoofax.core.processing;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.spoofax.core.language.LanguageChange;

public interface IProcessor<P, A, T> {
    public abstract ITask<BuildOutput<P, A, T>> build(BuildInput input);

    public abstract ITask<?> clean(FileObject location);

    public abstract ITask<?> metaBuild(FileObject location);
    
    public abstract ITask<?> metaClean(FileObject location);

    public abstract ITask<?> languageChange(LanguageChange change);
}
