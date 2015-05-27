package org.metaborg.spoofax.eclipse.build;

import java.util.Collection;

import org.metaborg.spoofax.core.processing.IdentifiedResourceChange;
import org.metaborg.spoofax.core.resource.IResourceChange;

public class BuildChanges {
    public final Collection<IResourceChange> parseTableChanges;
    public final Collection<IdentifiedResourceChange> languageResourceChanges;


    public BuildChanges(Collection<IResourceChange> parseTableChanges,
        Collection<IdentifiedResourceChange> languageResourceChanges) {
        this.languageResourceChanges = languageResourceChanges;
        this.parseTableChanges = parseTableChanges;
    }


    public boolean isEmpty() {
        return parseTableChanges.isEmpty() && languageResourceChanges.isEmpty();
    }
}
