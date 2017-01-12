package org.metaborg.spoofax.core.analysis.constraint;

import java.io.Serializable;

import org.metaborg.core.source.ISourceRegion;

class SerializableSourceLocation implements Serializable {

    private static final long serialVersionUID = 42L;

    private final String resource;
    private final ISourceRegion sourceRegion;

    public SerializableSourceLocation(String resource, ISourceRegion sourceRegion) {
        super();
        this.resource = resource;
        this.sourceRegion = sourceRegion;
    }

    public String getResource() {
        return resource;
    }

    public ISourceRegion getSourceRegion() {
        return sourceRegion;
    }

}