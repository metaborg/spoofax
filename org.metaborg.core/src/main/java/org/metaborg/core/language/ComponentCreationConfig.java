package org.metaborg.core.language;

import java.util.Collection;
import java.util.LinkedList;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.config.ILanguageComponentConfig;

public class ComponentCreationConfig {
    protected final Collection<IFacet> facets = new LinkedList<>();

    public final LanguageIdentifier identifier;
    public final FileObject location;
    public final Iterable<LanguageContributionIdentifier> implIds;
    public final ILanguageComponentConfig config;


    public ComponentCreationConfig(LanguageIdentifier identifier, FileObject location,
        Iterable<LanguageContributionIdentifier> implIds, ILanguageComponentConfig config) {
        this.identifier = identifier;
        this.location = location;
        this.implIds = implIds;
        this.config = config;
    }


    public void addFacet(IFacet facet) {
        facets.add(facet);
    }
}
