package org.metaborg.core.language;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import com.google.common.base.Strings;
import com.google.common.collect.ComparisonChain;

/**
 * Representation for the version of a language. Follows the Maven versioning style.
 */
public class LanguageVersion implements Comparable<LanguageVersion>, Serializable {
    private static final long serialVersionUID = -4814753959508772739L;
    private static final String SNAPSHOT = "SNAPSHOT";
    private static final Pattern pattern = Pattern.compile("(\\d+)(?:\\.(\\d+)(?:\\.(\\d+))?)?(?:(?:\\-)(.+))?");

    public static final String errorDescription =
        "must consist of 1, 2, or 3 numbers separated by dots, optionally followed by a -qualifier string (e.g. 1.0.0-SNAPSHOT)";

    public final int major;
    public final @Nullable Integer minor;
    public final @Nullable Integer patch;
    public final @Nullable String qualifier;


    public LanguageVersion(int major) {
        this(major, null, null, null);
    }

    public LanguageVersion(int major, int minor) {
        this(major, minor, null, null);
    }

    public LanguageVersion(int major, int minor, int patch) {
        this(major, minor, patch, null);
    }

    public LanguageVersion(int major, @Nullable Integer minor, @Nullable Integer patch, @Nullable String qualifier) {
        this.major = major;
        this.minor = minor;
        this.patch = patch;
        this.qualifier = qualifier;
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

        final String majorStr = matcher.group(1);
        if(majorStr == null) {
            // Should never happen according to regex pattern, but check just in case.
            throw new IllegalArgumentException(
                "Invalid version string " + version + ", major version may not be empty");
        }
        final String minorStr = matcher.group(2);
        final String patchStr = matcher.group(3);

        final int major = Integer.parseInt(majorStr);
        final Integer minor = (minorStr == null || minorStr.isEmpty()) ? null : Integer.parseInt(minorStr);
        final Integer patch = (patchStr == null || patchStr.isEmpty()) ? null : Integer.parseInt(patchStr);
        final String qualifier = matcher.group(4);

        return new LanguageVersion(major, minor, patch, qualifier);
    }


    @Override public int compareTo(LanguageVersion other) {
        // @formatter:off
        return ComparisonChain.start()
            .compare(this.major, other.major)
            .compare(this.minor, other.minor)
            .compare(this.patch, other.patch)
            .compare(this.qualifier, other.qualifier, new Comparator<String>() {
                @Override public int compare(@Nullable String left, @Nullable String right) {
                    final boolean leftNull = Strings.isNullOrEmpty(left);
                    final boolean rightNull = Strings.isNullOrEmpty(right);
                    if(leftNull && rightNull) {
                        return 0;
                    }
                    
                    final boolean leftSnapshot = SNAPSHOT.equalsIgnoreCase(left);
                    final boolean rightSnapshot = SNAPSHOT.equalsIgnoreCase(right);
                    if(leftSnapshot && rightSnapshot) {
                        return 0;  
                    }
                    
                    if(leftNull) {
                        return rightSnapshot ? -1 : 1;
                    } else if(rightNull) {
                        return leftSnapshot ? 1 : -1;
                    } else {
                        if(leftSnapshot) {
                            return 1;
                        } else if(rightSnapshot) {
                            return -1;
                        } else {
                            return left.compareTo(right);
                        }
                    }
                }})
            .result()
            ;
        // @formatter:on
    }

    @Override public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + major;
        if(minor != null) {
            result = prime * result + minor;
        }
        if(patch != null) {
            result = prime * result + patch;
        }
        if(qualifier != null) {
            result = prime * result + qualifier.hashCode();
        }
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
        if(!Objects.equals(minor, other.minor))
            return false;
        if(!Objects.equals(patch, other.patch))
            return false;
        if(!Strings.nullToEmpty(qualifier).equalsIgnoreCase(Strings.nullToEmpty(other.qualifier)))
            return false;
        return true;
    }

    @Override public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(major);
        if(minor != null) {
            sb.append('.');
            sb.append(minor);
            if(patch != null) {
                sb.append('.');
                sb.append(patch);
            }
        }
        if(qualifier != null) {
            sb.append('-');
            sb.append(qualifier);
        }
        return sb.toString();
    }
}
