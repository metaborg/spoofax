package org.metaborg.core.language;

import java.io.Serializable;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.metaborg.core.MetaborgConstants;
import org.metaborg.core.config.LanguageNameDeserializer;
import org.metaborg.core.config.LanguageNameSerializer;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.collect.ComparisonChain;

@JsonSerialize(using = LanguageNameSerializer.class)
@JsonDeserialize(using = LanguageNameDeserializer.class)
public class LanguageName implements Comparable<LanguageName>, Serializable {
    private static final long serialVersionUID = 8892997161544718124L;

    static final Pattern partialPattern = Pattern.compile("(?:(" + LanguageNameUtils.idPattern + "):)?(" + LanguageNameUtils.idPattern + ")");
    static final Pattern fullPattern = Pattern.compile("(?:(" + LanguageNameUtils.idPattern + "):)(" + LanguageNameUtils.idPattern + ")");


    public final String groupId;
    public final String id;


    public LanguageName(String groupId, String id) {
        this.groupId = groupId;
        this.id = id;
    }

    public LanguageName(LanguageName identifier) {
        this(identifier.groupId, identifier.id);
    }

    public boolean valid() {
        return LanguageNameUtils.validId(groupId) && LanguageNameUtils.validId(id);
    }

    public String groupId() {
        return groupId;
    }

    public String id() {
        return id;
    }


    public static boolean valid(String identifier) {
        final Matcher matcher = partialPattern.matcher(identifier);
        if(!matcher.matches()) {
            return false;
        }
        return true;
    }

    public static LanguageName parse(String identifier) {
        return parse(identifier, partialPattern);
    }

    public static LanguageName parseFull(String identifier) {
        return parse(identifier, fullPattern);
    }

    private static LanguageName parse(String identifier, Pattern pattern) {
        final Matcher matcher = pattern.matcher(identifier);
        if(!matcher.matches()) {
            throw new IllegalArgumentException("Invalid language identifier " + identifier
                    + ", expected groupId:id where characters " + LanguageNameUtils.errorDescription);
        }

        String groupId = matcher.group(1);
        if(groupId == null || groupId.isEmpty()) {
            groupId = MetaborgConstants.METABORG_GROUP_ID;
        }

        final String id = matcher.group(2);

        return new LanguageName(groupId, id);
    }


    @Override public int hashCode() {
        return Objects.hash(groupId, id);
    }

    @Override public boolean equals(Object obj) {
        if(this == obj)
            return true;
        if(obj == null)
            return false;
        if(getClass() != obj.getClass())
            return false;
        final LanguageName other = (LanguageName) obj;
        if(!groupId.equals(other.groupId))
            return false;
        if(!id.equals(other.id))
            return false;
        return true;
    }

    @Override public int compareTo(LanguageName other) {
        // @formatter:off
        return ComparisonChain.start()
            .compare(this.groupId, other.groupId)
            .compare(this.id, other.id)
            .result()
            ;
        // @formatter:on
    }

    @Override public String toString() {
        return groupId + ":" + id;
    }

    public String toFileString() {
        return id;
    }

    public String toFullFileString() {
        return groupId + "-" + id;
    }

}