package org.metaborg.spoofax.core.messages;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;

public class SourceLocation implements ISourceLocation {
    public final ISourceRegion region;
    public final @Nullable FileObject resource;


    public SourceLocation(ISourceRegion region, @Nullable FileObject resource) {
        this.region = region;
        this.resource = resource;
    }


    @Override public ISourceRegion region() {
        return region;
    }

    @Override public @Nullable FileObject resource() {
        return resource;
    }


    @Override public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + region.hashCode();
        result = prime * result + ((resource == null) ? 0 : resource.hashCode());
        return result;
    }

    @Override public boolean equals(Object obj) {
        if(this == obj)
            return true;
        if(obj == null)
            return false;
        if(getClass() != obj.getClass())
            return false;
        final SourceLocation other = (SourceLocation) obj;
        if(!region.equals(other.region))
            return false;
        if(resource == null) {
            if(other.resource != null)
                return false;
        } else if(!resource.equals(other.resource))
            return false;
        return true;
    }

    @Override public String toString() {
        return String.format("%s in %s", region, resource);
    }
}
