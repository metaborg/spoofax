package org.metaborg.spoofax.core.context;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.spoofax.core.language.ILanguage;

public class ResourceContextStrategy implements IContextStrategy {
    private static final long serialVersionUID = 8788600621990141023L;
    
	public static final String name = "resource";


    @Override public ContextIdentifier get(FileObject resource, ILanguage language) {
        return new ContextIdentifier(resource, language);
    }
}
