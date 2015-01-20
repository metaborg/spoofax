package org.metaborg.spoofax.core.context;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.spoofax.core.language.ILanguage;

public interface IContext {
    public abstract ILanguage language();

    public abstract FileObject location();
}
