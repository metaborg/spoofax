package org.metaborg.core.project.settings;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.language.LanguageContributionIdentifier;
import org.metaborg.core.language.LanguageIdentifier;
import org.metaborg.core.language.LanguageVersion;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.common.collect.Lists;

@Deprecated
public class YAMLLegacyProjectSettingsSerializer {
    public static ILegacyProjectSettings read(FileObject file) throws IOException {
        try(final InputStream stream = file.getContent().getInputStream()) {
            final ObjectMapper mapper = mapper();
            return mapper.readValue(stream, ILegacyProjectSettings.class);
        }
    }

    public static void write(FileObject file, ILegacyProjectSettings settings) throws IOException {
        file.createFile();
        try(final OutputStream stream = file.getContent().getOutputStream()) {
            final ObjectMapper mapper = mapper();
            mapper.writeValue(stream, settings);
        }
    }

    private static ObjectMapper mapper() {
        final SimpleModule module = new SimpleModule();
        module.addSerializer(ILegacyProjectSettings.class, new ProjectSettingsSerializer());
        module.addDeserializer(ILegacyProjectSettings.class, new ProjectSettingsDeserializer());

        final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        mapper.registerModule(module);

        return mapper;
    }
}

@Deprecated
class ProjectSettingsSerializer extends JsonSerializer<ILegacyProjectSettings> {
    @Override public void serialize(ILegacyProjectSettings value, JsonGenerator gen, SerializerProvider serializers)
        throws IOException {
        gen.writeStartObject();
        gen.writeFieldName("identifier");
        serialize(value.identifier(), gen);
        gen.writeFieldName("name");
        gen.writeString(value.name());
        gen.writeFieldName("compileDependencies");
        gen.writeStartArray();
        for(LanguageIdentifier identifier : value.compileDependencies()) {
            serialize(identifier, gen);
        }
        gen.writeEndArray();
        gen.writeFieldName("runtimeDependencies");
        gen.writeStartArray();
        for(LanguageIdentifier identifier : value.runtimeDependencies()) {
            serialize(identifier, gen);
        }
        gen.writeEndArray();
        gen.writeFieldName("languageContributions");
        gen.writeStartArray();
        for(LanguageContributionIdentifier identifier : value.languageContributions()) {
            serialize(identifier, gen);
        }
        gen.writeEndArray();
        gen.writeEndObject();
    }

    private void serialize(LanguageVersion version, JsonGenerator gen) throws IOException {
        gen.writeStartObject();
        gen.writeFieldName("major");
        gen.writeNumber(version.major());
        gen.writeFieldName("minor");
        gen.writeNumber(version.minor());
        gen.writeFieldName("patch");
        gen.writeNumber(version.patch());
        gen.writeFieldName("qualifier");
        gen.writeString(version.qualifier());
        gen.writeEndObject();
    }

    private void serialize(LanguageIdentifier identifier, JsonGenerator gen) throws IOException {
        gen.writeStartObject();
        gen.writeFieldName("groupId");
        gen.writeString(identifier.groupId);
        gen.writeFieldName("id");
        gen.writeString(identifier.id);
        gen.writeFieldName("version");
        serialize(identifier.version, gen);
        gen.writeEndObject();
    }

    private void serialize(LanguageContributionIdentifier identifier, JsonGenerator gen) throws IOException {
        gen.writeStartObject();
        gen.writeFieldName("identifier");
        serialize(identifier.id, gen);
        gen.writeFieldName("name");
        gen.writeString(identifier.name);
        gen.writeEndObject();
    }
}

@Deprecated
class ProjectSettingsDeserializer extends JsonDeserializer<ILegacyProjectSettings> {
    @Override public ILegacyProjectSettings deserialize(JsonParser parser, DeserializationContext ctxt)
        throws IOException {
        final JsonNode root = parser.getCodec().readTree(parser);

        final LanguageIdentifier identifier = identifier(root.get("identifier"));
        final String name = asText(root.get("name"));
        final Collection<LanguageIdentifier> compileDependencies = Lists.newLinkedList();
        for(JsonNode node : root.get("compileDependencies")) {
            compileDependencies.add(identifier(node));
        }
        final Collection<LanguageIdentifier> runtimeDependencies = Lists.newLinkedList();
        for(JsonNode node : root.get("runtimeDependencies")) {
            runtimeDependencies.add(identifier(node));
        }
        final Collection<LanguageContributionIdentifier> languageContributions = Lists.newLinkedList();
        for(JsonNode node : root.get("languageContributions")) {
            languageContributions.add(contributionIdentifier(node));
        }
        return new LegacyProjectSettings(identifier, name, compileDependencies, runtimeDependencies,
            languageContributions);
    }

    private LanguageVersion version(JsonNode root) {
        final int major = asInt(root.get("major"));
        final int minor = asInt(root.get("minor"));
        final int patch = asInt(root.get("patch"));
        final String qualifier = asText(root.get("qualifier"));
        return new LanguageVersion(major, minor, patch, qualifier);
    }

    private LanguageIdentifier identifier(JsonNode root) {
        final String groupId = asText(root.get("groupId"));
        final String id = asText(root.get("id"));
        final LanguageVersion version = version(root.get("version"));
        return new LanguageIdentifier(groupId, id, version);
    }

    private LanguageContributionIdentifier contributionIdentifier(JsonNode root) {
        final LanguageIdentifier identifier = identifier(root.get("identifier"));
        final String name = asText(root.get("name"));
        return new LanguageContributionIdentifier(identifier, name);
    }

    private String asText(JsonNode node) {
        return asText(node, "");
    }

    private String asText(JsonNode node, String defaultValue) {
        if(node == null)
            return defaultValue;
        return node.asText(defaultValue);
    }

    private int asInt(JsonNode node) {
        return asInt(node, 0);
    }

    private int asInt(JsonNode node, int defaultValue) {
        if(node == null)
            return defaultValue;
        return node.asInt(defaultValue);
    }
}
