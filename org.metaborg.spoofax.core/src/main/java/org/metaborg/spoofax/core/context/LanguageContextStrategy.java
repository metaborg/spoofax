package org.metaborg.spoofax.core.context;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.spoofax.core.language.ILanguage;

public class LanguageContextStrategy implements IContextStrategy {
    public static final String name = "language";


    @Override public ContextIdentifier get(FileObject resource, ILanguage language) {
        return new ContextIdentifier(language.location(), language);
    }
}
