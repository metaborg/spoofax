package org.metaborg.core.language;

import java.util.Collection;

import org.apache.commons.vfs2.FileObject;

import com.google.common.collect.Lists;

public class LanguageCreationRequest {
    protected final Collection<IFacet> facets = Lists.newLinkedList();

    public final LanguageIdentifier identifier;
    public final FileObject location;
    public final Iterable<LanguageContributionIdentifier> implIds;


    public LanguageCreationRequest(LanguageIdentifier identifier, FileObject location,
        Iterable<LanguageContributionIdentifier> implIds) {
        this.identifier = identifier;
        this.location = location;
        this.implIds = implIds;
    }


    public void addFacet(IFacet facet) {
        facets.add(facet);
    }
}
