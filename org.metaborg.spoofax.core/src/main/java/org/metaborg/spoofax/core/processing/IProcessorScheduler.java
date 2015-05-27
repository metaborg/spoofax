package org.metaborg.spoofax.core.processing;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.spoofax.core.language.LanguageChange;
import org.metaborg.spoofax.core.task.ITask;

public interface IProcessorScheduler {
    public abstract ITask<?> build(BuildInput input);

    public abstract ITask<?> metaBuild(FileObject location);

    public abstract ITask<?> languageChange(LanguageChange change);
}
