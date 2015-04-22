package org.metaborg.spoofax.core.language;

import java.io.Serializable;

import com.google.common.collect.ComparisonChain;

/**
 * Representation for the version of a language. Follows the versioning style of OSGI to be compatible with Eclipse
 * plugins.
 */
public class LanguageVersion implements Comparable<LanguageVersion>, Serializable {
    private static final long serialVersionUID = -4814753959508772739L;

    public final int major;
    public final int minor;
    public final int patch;
    public final int qualifier;


    public LanguageVersion(int major, int minor, int patch, int qualifier) {
        this.major = major;
        this.minor = minor;
        this.patch = patch;
        this.qualifier = qualifier;
    }


    @Override public int compareTo(LanguageVersion other) {
        // @formatter:off
        return ComparisonChain.start()
            .compare(this.major, other.major)
            .compare(this.minor, other.minor)
            .compare(this.patch, other.patch)
            .compare(this.qualifier, other.qualifier)
            .result();
        // @formatter:on
    }


    @Override public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + major;
        result = prime * result + minor;
        result = prime * result + patch;
        result = prime * result + qualifier;
        return result;
    }

    @Override public boolean equals(Object obj) {
        if(this == obj)
            return true;
        if(obj == null)
            return false;
        if(getClass() != obj.getClass())
            return false;
        final LanguageVersion other = (LanguageVersion) obj;
        if(major != other.major)
            return false;
        if(minor != other.minor)
            return false;
        if(patch != other.patch)
            return false;
        if(qualifier != other.qualifier)
            return false;
        return true;
    }

    @Override public String toString() {
        return major + "." + minor + "." + patch + "-" + qualifier;
    }
}
