package org.metaborg.core.config;

import java.io.IOException;

import org.metaborg.core.language.LanguageName;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

public class LanguageNameDeserializer extends StdDeserializer<LanguageName> {
    private static final long serialVersionUID = -7890806146378427526L;


    public LanguageNameDeserializer() {
        super(LanguageName.class);
    }


    @Override public LanguageName deserialize(final JsonParser parser, final DeserializationContext context)
        throws IOException {
        final ObjectMapper mapper = (ObjectMapper) parser.getCodec();
        final JsonNode root = mapper.readTree(parser);

        return LanguageName.parse(root.asText());
    }

}