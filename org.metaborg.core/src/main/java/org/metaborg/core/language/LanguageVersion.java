package org.metaborg.core.language;

import jakarta.annotation.Nullable;
import java.io.Serializable;
import java.util.Comparator;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import org.metaborg.util.Strings;
import org.metaborg.util.order.ChainedComparison;

/**
 * Representation for the version of a language. Follows the Maven versioning style.
 */
public class LanguageVersion implements Comparable<LanguageVersion>, Serializable {
    private static final long serialVersionUID = 2L;
    private static final String SNAPSHOT = "snapshot";

    private final @Nullable Integer major;
    private final @Nullable Integer minor;
    private final @Nullable Integer incremental;
    private final @Nullable String qualifier;


    public LanguageVersion(int major) {
        this(major, null, null, null);
    }

    public LanguageVersion(int major, int minor) {
        this(major, minor, null, null);
    }

    public LanguageVersion(int major, int minor, int incremental) {
        this(major, minor, incremental, null);
    }

    public LanguageVersion(
        @Nullable Integer major, @Nullable Integer minor, @Nullable Integer incremental, @Nullable String qualifier) {
        this.major = major;
        this.minor = minor;
        this.incremental = incremental;
        this.qualifier = qualifier;
    }

    public static LanguageVersion parse(String version) {
        // Copied and edited from: https://github.com/apache/maven/blob/master/maven-artifact/src/main/java/org/apache/maven/artifact/versioning/DefaultArtifactVersion.java
        @Nullable Integer major = null;
        @Nullable Integer minor = null;
        @Nullable Integer incremental = null;
        @Nullable String qualifier = null;

        final String numericalPart;
        final int hyphenIndex = version.indexOf('-');
        if(hyphenIndex < 0) {
            numericalPart = version;
        } else {
            numericalPart = version.substring(0, hyphenIndex);
            // Qualifier is part of version starting from (including) "-".
            qualifier = version.substring(hyphenIndex);
        }

        if((!numericalPart.contains(".")) && !numericalPart.startsWith("0")) {
            try {
                major = Integer.valueOf(numericalPart);
            } catch(NumberFormatException e) {
                // Qualifier is the whole version, including "-".
                qualifier = version;
            }
        } else {
            boolean fallback = false;
            final StringTokenizer tok = new StringTokenizer(numericalPart, ".");
            try {
                major = getNextIntegerToken(tok);
                if(tok.hasMoreTokens()) {
                    minor = getNextIntegerToken(tok);
                }
                if(tok.hasMoreTokens()) {
                    incremental = getNextIntegerToken(tok);
                }
                if(tok.hasMoreTokens()) {
                    qualifier = tok.nextToken();
                    fallback = Pattern.compile("\\d+").matcher(qualifier).matches();
                }
                // String tokenizer won't detect these and ignores them.
                if(numericalPart.contains("..") || numericalPart.startsWith(".") || numericalPart.endsWith(".")) {
                    fallback = true;
                }
            } catch(NumberFormatException e) {
                fallback = true;
            }

            if(fallback) {
                // Qualifier is the whole version, including "-".
                qualifier = version;
                major = null;
                minor = null;
                incremental = null;
            }
        }

        return new LanguageVersion(major, minor, incremental, qualifier);
    }

    private static Integer getNextIntegerToken(StringTokenizer tok) {
        // Copied from: https://github.com/apache/maven/blob/master/maven-artifact/src/main/java/org/apache/maven/artifact/versioning/DefaultArtifactVersion.java
        try {
            final String s = tok.nextToken();
            if((s.length() > 1) && s.startsWith("0")) {
                throw new NumberFormatException("Number part has a leading 0: '" + s + "'");
            }
            return Integer.valueOf(s);
        } catch(NoSuchElementException e) {
            throw new NumberFormatException("Number is invalid");
        }
    }

    /**
     * @deprecated Always returns true because version is always valid.
     */
    @Deprecated
    public static boolean valid(String version) {
        return true;
    }


    @Override public int compareTo(LanguageVersion other) {
        // @formatter:off
        return new ChainedComparison()
            .compare(this.major, other.major, Comparator.nullsFirst(Comparator.naturalOrder()))
            .compare(this.minor, other.minor, Comparator.nullsFirst(Comparator.naturalOrder()))
            .compare(this.incremental, other.incremental, Comparator.nullsFirst(Comparator.naturalOrder()))
            .compare(this.qualifier, other.qualifier, (left,right) -> {
                final boolean leftNull = Strings.isNullOrEmpty(left);
                final boolean rightNull = Strings.isNullOrEmpty(right);
                if(leftNull && rightNull) {
                    return 0;
                }

                final boolean leftSnapshot = !leftNull && left.toLowerCase().contains(SNAPSHOT);
                final boolean rightSnapshot = !rightNull && right.toLowerCase().contains(SNAPSHOT);
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
            })
            .result();
        // @formatter:on
    }

    @Override public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        final LanguageVersion other = (LanguageVersion) o;
        if(major != null ? !major.equals(other.major) : other.major != null) return false;
        if(minor != null ? !minor.equals(other.minor) : other.minor != null) return false;
        if(incremental != null ? !incremental.equals(other.incremental) : other.incremental != null) return false;
        return Strings.nullToEmpty(qualifier).equalsIgnoreCase(Strings.nullToEmpty(other.qualifier)); // Equals ignoring casing.
    }

    @Override public int hashCode() {
        int result = major != null ? major.hashCode() : 0;
        result = 31 * result + (minor != null ? minor.hashCode() : 0);
        result = 31 * result + (incremental != null ? incremental.hashCode() : 0);
        result = 31 * result + (qualifier != null ? qualifier.toLowerCase().hashCode() : 0); // Hash code of qualifier always in lowercase to ignore casing.
        return result;
    }

    @Override public String toString() {
        final StringBuilder sb = new StringBuilder();
        if(major != null) {
            sb.append(major);
        } else if(minor != null || incremental != null) {
            sb.append(0);
        }

        if(minor != null) {
            sb.append('.');
            sb.append(minor);
        } else if(incremental != null) {
            sb.append(0);
        }

        if(incremental != null) {
            sb.append('.');
            sb.append(incremental);
        }

        if(qualifier != null) {
            sb.append(qualifier);
        }

        return sb.toString();
    }
}
