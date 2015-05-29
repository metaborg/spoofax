package org.metaborg.spoofax.core.context;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.spoofax.core.language.ILanguage;

public interface ILanguagePathService {
    
    public Iterable<FileObject> getSources(IContext context, ILanguage language);

    public Iterable<FileObject> getIncludes(IContext context, ILanguage language);

}
