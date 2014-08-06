package org.metaborg.spoofax.core.language;

import org.apache.commons.vfs2.FileObject;

public interface ILanguageFacetFactory {
    public ILanguageFacet create(Iterable<FileObject> resources);
}
