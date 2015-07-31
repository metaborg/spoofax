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
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.common.collect.Lists;

public class YAMLProjectSettingsSerializer {
    public static IProjectSettings read(FileObject file) throws IOException {
        try(final InputStream stream = file.getContent().getInputStream()) {
            final ObjectMapper mapper = mapper();
            return mapper.readValue(stream, IProjectSettings.class);
        }
    }

    public static void write(FileObject file, IProjectSettings settings) throws IOException {
        file.createFile();
        try(final OutputStream stream = file.getContent().getOutputStream()) {
            final ObjectMapper mapper = mapper();
            mapper.writeValue(stream, settings);
        }
    }

    private static ObjectMapper mapper() {
        final SimpleModule module = new SimpleModule();
        module.addSerializer(IProjectSettings.class, new ProjectSettingsSerializer());
        module.addDeserializer(IProjectSettings.class, new ProjectSettingsDeserializer());

        final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        mapper.registerModule(module);

        return mapper;
    }
}

class ProjectSettingsSerializer extends JsonSerializer<IProjectSettings> {
    @Override public void serialize(IProjectSettings value, JsonGenerator gen, SerializerProvider serializers)
        throws IOException, JsonProcessingException {
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
        serialize(identifier.identifier, gen);
        gen.writeFieldName("name");
        gen.writeString(identifier.name);
        gen.writeEndObject();
    }
}

class ProjectSettingsDeserializer extends JsonDeserializer<IProjectSettings> {
    @Override public IProjectSettings deserialize(JsonParser parser, DeserializationContext ctxt) throws IOException,
        JsonProcessingException {
        final JsonNode root = parser.getCodec().readTree(parser);

        final LanguageIdentifier identifier = identifier(root.get("identifier"));
        final String name = root.get("name").asText();
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
        return new ProjectSettings(identifier, name, compileDependencies, runtimeDependencies, languageContributions);
    }

    private LanguageVersion version(JsonNode root) {
        final int major = root.get("major").asInt();
        final int minor = root.get("minor").asInt();
        final int patch = root.get("patch").asInt();
        final String qualifier = root.get("qualifier").asText();
        return new LanguageVersion(major, minor, patch, qualifier);
    }

    private LanguageIdentifier identifier(JsonNode root) {
        final String groupId = root.get("groupId").asText();
        final String id = root.get("id").asText();
        final LanguageVersion version = version(root.get("version"));
        return new LanguageIdentifier(groupId, id, version);
    }

    private LanguageContributionIdentifier contributionIdentifier(JsonNode root) {
        final LanguageIdentifier identifier = identifier(root.get("identifier"));
        final String name = root.get("name").asText();
        return new LanguageContributionIdentifier(identifier, name);
    }
}
