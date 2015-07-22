package org.metaborg.core.context;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.language.ILanguageImpl;

public class ResourceContextStrategy implements IContextStrategy {
    private static final long serialVersionUID = 8788600621990141023L;

    public static final String name = "resource";


    @Override public ContextIdentifier get(FileObject resource, ILanguageImpl language) {
        return new ContextIdentifier(resource, language);
    }
}
