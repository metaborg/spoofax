package org.metaborg.spoofax.core.processing;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.spoofax.core.language.ILanguage;
import org.metaborg.spoofax.core.resource.IResourceChange;

public class BuildInput {
    public final FileObject location;
    public final Iterable<ILanguage> languages;
    public final Iterable<IResourceChange> resourceChanges;


    public BuildInput(FileObject location, Iterable<ILanguage> languages, Iterable<IResourceChange> resourceChanges) {
        this.location = location;
        this.languages = languages;
        this.resourceChanges = resourceChanges;
    }

}
