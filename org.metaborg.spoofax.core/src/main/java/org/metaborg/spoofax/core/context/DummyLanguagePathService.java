package org.metaborg.spoofax.core.context;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.spoofax.core.language.ILanguage;
import org.metaborg.util.iterators.Iterables2;

public class DummyLanguagePathService implements ILanguagePathService {

    @Override
    public Iterable<FileObject> getSources(IContext context, ILanguage language) {
        return Iterables2.singleton(context.location());
    }

    @Override
    public Iterable<FileObject> getIncludes(IContext context, ILanguage language) {
        return Iterables2.empty();
    }
    
}
