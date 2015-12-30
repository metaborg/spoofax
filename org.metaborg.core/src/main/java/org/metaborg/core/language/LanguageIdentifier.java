package org.metaborg.core.language;

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.metaborg.core.project.settings.LanguageIdentifierJacksonDeserializer;
import org.metaborg.core.project.settings.LanguageIdentifierJacksonSerializer;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.collect.ComparisonChain;

@JsonSerialize(using = LanguageIdentifierJacksonSerializer.class)
@JsonDeserialize(using = LanguageIdentifierJacksonDeserializer.class)
public class LanguageIdentifier implements Comparable<LanguageIdentifier>, Serializable {
    private static final long serialVersionUID = 8892997161544718124L;
    
    private static final Pattern idPattern = Pattern.compile("[A-Za-z0-9._\\-]+");
    public static final String errorDescription = "may only contain characters, numbers, and _ - .";
    private static final Pattern fullPattern = Pattern.compile("(?:(" + idPattern + "):)?(" + idPattern + ")(?::(.+))?");
    public static final LanguageIdentifier EMPTY = new LanguageIdentifier("", "", LanguageVersion.BASELINE_VERSION);
    
    public final String groupId;
    public final String id;
    public final LanguageVersion version;


    public LanguageIdentifier(String groupId, String id, LanguageVersion version) {
        this.groupId = groupId;
        this.id = id;
        this.version = version;
    }

    public LanguageIdentifier(LanguageIdentifier identifier, LanguageVersion version) {
        this(identifier.groupId, identifier.id, version);
    }


    public boolean valid() {
        return validId(groupId) && validId(id);
    }


    public static boolean valid(String identifier) {
        final Matcher matcher = fullPattern.matcher(identifier);
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
        final Matcher matcher = fullPattern.matcher(identifier);
        if(!matcher.matches()) {
            throw new IllegalArgumentException("Invalid language identifier " + identifier);
        }

        String groupId = matcher.group(1);
        if(groupId == null || groupId.isEmpty()) {
            // GTODO: move this constant into a class
            groupId = "org.metaborg";
        }

        final String id = matcher.group(2);
        final String versionString = matcher.group(3);
        final LanguageVersion version = LanguageVersion.parse(versionString);

        return new LanguageIdentifier(groupId, id, version);
    }

    public static boolean validId(String id) {
        final Matcher matcher = idPattern.matcher(id);
        if(!matcher.matches()) {
            return false;
        }
        return true;
    }


    @Override public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + groupId.hashCode();
        result = prime * result + id.hashCode();
        result = prime * result + version.hashCode();
        return result;
    }

    @Override public boolean equals(Object obj) {
        if(this == obj)
            return true;
        if(obj == null)
            return false;
        if(getClass() != obj.getClass())
            return false;
        final LanguageIdentifier other = (LanguageIdentifier) obj;
        if(!groupId.equals(other.groupId))
            return false;
        if(!id.equals(other.id))
            return false;
        if(!version.equals(other.version))
            return false;
        return true;
    }

    @Override public int compareTo(LanguageIdentifier other) {
        // @formatter:off
        return ComparisonChain.start()
            .compare(this.groupId, other.groupId)
            .compare(this.id, other.id)
            .compare(this.version, other.version)
            .result()
            ;
        // @formatter:on
    }

    @Override public String toString() {
        return groupId + ":" + id + ":" + version;
    }
}
