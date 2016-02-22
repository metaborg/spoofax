package org.metaborg.core.language;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.collect.ComparisonChain;

/**
 * Representation for the version of a language. Follows the Maven versioning style.
 */
public class LanguageVersion implements Comparable<LanguageVersion>, Serializable {
    private static final long serialVersionUID = -4814753959508772739L;
    private static final String SNAPSHOT = "SNAPSHOT";
    private static final Pattern pattern = Pattern.compile("(\\d+)?(?:\\.(\\d+)(?:\\.(\\d+))?)?(?:(?:\\-)(.+))?");
    
    public static final String errorDescription = "must consist of 1-3 numbers separated by dots, optionally followed by a -qualifier string (e.g. 1.0.0-SNAPSHOT)";

    // BOOTSTRAPPING: The version of a baseline language.
    public static final LanguageVersion BASELINE_VERSION = new LanguageVersion(0, 0, 0, "");

    public final int major;
    public final int minor;
    public final int patch;
    public final String qualifier;

    
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


    public static boolean valid(String version) {
        final Matcher matcher = pattern.matcher(version);
        if(!matcher.matches()) {
            return false;
        }
        return true;
    }

    public static LanguageVersion parse(String version) {
        final Matcher matcher = pattern.matcher(version);
        if(!matcher.matches()) {
            throw new IllegalArgumentException("Invalid version string " + version);
        }

        String major = matcher.group(1);
        major = major == null || major.isEmpty() ? "0" : major;

        String minor = matcher.group(2);
        minor = minor == null || minor.isEmpty() ? "0" : minor;

        String patch = matcher.group(3);
        patch = patch == null || patch.isEmpty() ? "0" : patch;

        String qualifier = matcher.group(4);
        qualifier = qualifier == null || qualifier.isEmpty() ? "" : qualifier;

        return new LanguageVersion(Integer.parseInt(major), Integer.parseInt(minor), Integer.parseInt(patch), qualifier);
    }


    @Override public int compareTo(LanguageVersion other) {
        // @formatter:off
        return ComparisonChain.start()
            .compare(this.major, other.major)
            .compare(this.minor, other.minor)
            .compare(this.patch, other.patch)
            .compare(this.qualifier, other.qualifier, new Comparator<String>() {
                @Override public int compare(String qualifier, String other) {
                    int result = qualifier.compareToIgnoreCase(other);
                    if(result != 0) {
                        if(SNAPSHOT.equalsIgnoreCase(qualifier)) {
                            return 1;
                        } else if(SNAPSHOT.equalsIgnoreCase(other)) {
                            return -1;
                        } else {
                            return result;
                        }
                    }
                    return 0;
                }})
            .result()
            ;
        // @formatter:on
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
        if(!qualifier.equalsIgnoreCase(other.qualifier))
            return false;
        return true;
    }

    @Override public String toString() {
        return String.format("%d.%d.%d%s", major, minor, patch, (qualifier.isEmpty() ? "" : ("-" + qualifier)));
    }
}
