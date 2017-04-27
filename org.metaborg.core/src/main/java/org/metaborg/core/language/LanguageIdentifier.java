package org.metaborg.core.language;

import java.io.Serializable;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.metaborg.core.MetaborgConstants;
import org.metaborg.core.config.LanguageIdentifierDeserializer;
import org.metaborg.core.config.LanguageIdentifierSerializer;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.collect.ComparisonChain;

@JsonSerialize(using = LanguageIdentifierSerializer.class)
@JsonDeserialize(using = LanguageIdentifierDeserializer.class)
public class LanguageIdentifier implements Comparable<LanguageIdentifier>, Serializable {
    private static final long serialVersionUID = 8892997161544718124L;

    private static final Pattern partialPattern = Pattern.compile(LanguageName.partialPattern + "(?::(.+))?");
    private static final Pattern fullPattern = Pattern.compile(LanguageName.fullPattern + "(?::(.+))");


    private final LanguageName name;
    private final LanguageVersion version;


    public LanguageIdentifier(String groupId, String id, LanguageVersion version) {
        this(new LanguageName(groupId, id), version);
    }

    public LanguageIdentifier(LanguageName name, LanguageVersion version) {
        this.name = name;
        this.version = version;
    }

    public boolean valid() {
        return name.valid();
    }

    public LanguageName name() {
        return name;
    }
    
    public String groupId() {
        return name.groupId();
    }

    public String id() {
        return name.id();
    }

    public LanguageVersion version() {
        return version;
    }


    public static boolean valid(String identifier) {
        final Matcher matcher = partialPattern.matcher(identifier);
        if(!matcher.matches()) {
            return false;
        }
        final String versionString = matcher.group(3);
        if(!LanguageVersion.valid(versionString)) {
            return false;
        }
        return true;
    }

    public static LanguageIdentifier parse(String identifier) {
        return parse(identifier, partialPattern);
    }

    public static LanguageIdentifier parseFull(String identifier) {
        return parse(identifier, fullPattern);
    }

    private static LanguageIdentifier parse(String identifier, Pattern pattern) {
        final Matcher matcher = pattern.matcher(identifier);
        if(!matcher.matches()) {
            throw new IllegalArgumentException("Invalid language identifier " + identifier
                    + ", expected groupId:id:version where characters " + LanguageNameUtils.errorDescription);
        }

        String groupId = matcher.group(1);
        if(groupId == null || groupId.isEmpty()) {
            groupId = MetaborgConstants.METABORG_GROUP_ID;
        }

        final String id = matcher.group(2);
        final String versionString = matcher.group(3);
        final LanguageVersion version;
        try {
            version = LanguageVersion.parse(versionString);
        } catch(IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Invalid version in language identifier " + identifier + ", " + LanguageVersion.errorDescription);
        }

        return new LanguageIdentifier(groupId, id, version);
    }


    @Override public int hashCode() {
        return Objects.hash(name, version);
    }

    @Override public boolean equals(Object obj) {
        if(this == obj)
            return true;
        if(obj == null)
            return false;
        if(getClass() != obj.getClass())
            return false;
        final LanguageIdentifier other = (LanguageIdentifier) obj;
        if(!name.equals(other.name))
            return false;
        if(!version.equals(other.version))
            return false;
        return true;
    }

    @Override public int compareTo(LanguageIdentifier other) {
        // @formatter:off
        return ComparisonChain.start()
            .compare(this.name, other.name)
            .compare(this.version, other.version)
            .result()
            ;
        // @formatter:on
    }

    @Override public String toString() {
        return name + ":" + version;
    }

    public String toFileString() {
        return name.toFileString() + "-" + version;
    }

    public String toFullFileString() {
        return name.toFullFileString() + "-" + version;
    }

}