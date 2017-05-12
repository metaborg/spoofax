package org.metaborg.core.config;

import java.io.IOException;

import org.metaborg.core.language.LanguageIdentifier;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

public class LanguageIdentifierDeserializer extends StdDeserializer<LanguageIdentifier> {
    private static final long serialVersionUID = -7890806146378427526L;


    public LanguageIdentifierDeserializer() {
        super(LanguageIdentifier.class);
    }


    @Override public LanguageIdentifier deserialize(final JsonParser parser, final DeserializationContext context)
        throws IOException {
        final ObjectMapper mapper = (ObjectMapper) parser.getCodec();
        final JsonNode root = mapper.readTree(parser);

        return LanguageIdentifier.parse(root.asText());
    }

}