package org.metaborg.core.project.settings;

import java.io.IOException;

import org.metaborg.core.language.LanguageIdentifier;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

/**
 * Deserializer for the {@link LanguageIdentifier} class.
 */
@Deprecated
public final class LegacyLanguageIdentifierJacksonDeserializer extends StdDeserializer<LanguageIdentifier> {

    public LegacyLanguageIdentifierJacksonDeserializer() {
        super(LanguageIdentifier.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LanguageIdentifier deserialize(final JsonParser parser, final DeserializationContext context) throws IOException {
        final ObjectMapper mapper = (ObjectMapper)parser.getCodec();
        final JsonNode root = mapper.readTree(parser);

        return LanguageIdentifier.parse(root.asText());
    }
}
