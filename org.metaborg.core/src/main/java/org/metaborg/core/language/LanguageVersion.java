package org.metaborg.core.language;

import java.io.Serializable;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.collect.ComparisonChain;

/**
 * Representation for the version of a language. Follows the versioning style of OSGI to be compatible with Eclipse
 * plugins.
 */
public class LanguageVersion implements Comparable<LanguageVersion>, Serializable {
    private static final long serialVersionUID = -4814753959508772739L;
    private static final String SNAPSHOT = "SNAPSHOT";

    private final int major;
    private final int minor;
    private final int patch;
    private final String qualifier;

    public LanguageVersion(int major, int minor, int patch, String qualifier) {
        this.major = major;
        this.minor = minor;
        this.patch = patch;
        this.qualifier = qualifier;
    }

    public int major() {
        return major;
    }

    public int minor() {
        return minor;
    }

    public int patch() {
        return patch;
    }

    public String qualifier() {
        return qualifier;
    }


    @Override public int compareTo(LanguageVersion other) {
        // @formatter:off
        int result = ComparisonChain.start()
            .compare(this.major, other.major)
            .compare(this.minor, other.minor)
            .compare(this.patch, other.patch)
            .result();
        // @formatter:on
        if(result == 0) {
            result = this.qualifier.compareToIgnoreCase(other.qualifier);
            if(result != 0) {
                if(SNAPSHOT.equalsIgnoreCase(this.qualifier)) {
                    result = -1;
                } else if(SNAPSHOT.equalsIgnoreCase(this.qualifier)) {
                    result = 1;
                }
            }
        }
        return result;
    }

    @Override public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + major;
        result = prime * result + minor;
        result = prime * result + patch;
        result = prime * result + Objects.hashCode(qualifier);
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
        if(!qualifier.equals(other.qualifier))
            return false;
        return true;
    }

    @Override public String toString() {
        return String.format("%d.%d.%d%s", major, minor, patch, (qualifier.isEmpty() ? "" : ("-" + qualifier)));
    }

    private static final Pattern VERSION_PATTERN = Pattern
        .compile("((\\d+)(\\.(\\d+)(\\.(\\d+))?)?(-(\\w[\\w\\-]*))?)?");

    public static boolean valid(String version) {
        final Matcher matcher = VERSION_PATTERN.matcher(version);
        if(!matcher.matches()) {
            return false;
        }
        return true;
    }

    public static LanguageVersion parse(String version) {
        final Matcher matcher = VERSION_PATTERN.matcher(version);
        if(!matcher.matches()) {
            throw new IllegalArgumentException("Invalid version string " + version);
        }

        String major = matcher.group(2);
        major = major == null || major.isEmpty() ? "0" : major;

        String minor = matcher.group(4);
        minor = minor == null || minor.isEmpty() ? "0" : minor;

        String patch = matcher.group(6);
        patch = patch == null || patch.isEmpty() ? "0" : patch;

        String qualifier = matcher.group(8);
        qualifier = qualifier == null || qualifier.isEmpty() ? "" : qualifier;

        return new LanguageVersion(Integer.parseInt(major), Integer.parseInt(minor), Integer.parseInt(patch), qualifier);
    }
}
