package org.metaborg.spoofax.core.context;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.spoofax.core.language.ILanguage;

public class SpoofaxContext implements ISpoofaxContext {
    private final ILanguage language;
    private final FileObject location;


    public SpoofaxContext(ILanguage language, FileObject location) {
        this.language = language;
        this.location = location;
    }


    @Override public ILanguage language() {
        return language;
    }

    @Override public FileObject location() {
        return location;
    }
}
